package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.core.operate.OperateService;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.common.util.SecurityUtil;
import cn.chenjun.cloud.management.config.ApplicationConfig;
import cn.chenjun.cloud.management.data.dao.*;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.servcie.AllocateService;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.servcie.TaskService;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.util.*;
import cn.chenjun.cloud.management.websocket.listen.context.HostContext;
import cn.chenjun.cloud.management.websocket.manager.HostClientManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author chenjun
 */
@Slf4j
public abstract class AbstractOperateService<T extends BaseOperateParam, V extends ResultUtil> implements OperateService {

    @Autowired
    protected GuestDao guestDao;
    @Autowired
    protected GuestNetworkDao guestNetworkDao;
    @Autowired
    protected HostDao hostDao;
    @Autowired
    protected NetworkDao networkDao;
    @Autowired
    protected StorageDao storageDao;

    @Autowired
    protected TemplateDao templateDao;
    @Autowired
    protected TemplateVolumeDao templateVolumeDao;
    @Autowired
    protected VolumeDao volumeDao;
    @Autowired
    protected SchemeDao schemeDao;
    @Autowired
    protected AllocateService allocateService;
    @Autowired
    @Lazy
    protected TaskService taskService;
    @Autowired
    protected ConfigService configService;
    @Autowired
    protected RestTemplate restTemplate;
    @Autowired
    protected RedissonClient redissonClient;

    @Autowired
    @Qualifier("workExecutorService")
    protected ScheduledThreadPoolExecutor executor;
    @Autowired
    protected ApplicationConfig applicationConfig;

    protected static String getVolumePath(StorageEntity storage, String volumeName) {
        String path;
        switch (storage.getType()) {
            case Constant.StorageType.NFS:
            case Constant.StorageType.GLUSTERFS:
            case Constant.StorageType.LOCAL:
                path = storage.getMountPath() + "/" + volumeName;
                break;
            case Constant.StorageType.CEPH_RBD:
                Map<String, Object> param = GsonBuilderUtil.create().fromJson(storage.getParam(), new TypeToken<Map<String, Object>>() {
                }.getType());
                String pool = param.get("pool").toString();
                path = "rbd:" + pool + "/" + volumeName;
                break;
            default:
                throw new CodeException(ErrorCode.STORAGE_NOT_SUPPORT, "未知的存储池类型:" + storage.getType());
        }
        return path;
    }

    @Override
    public boolean supports(@NonNull Integer type) {
        return this.getType() == type;
    }

    @SuppressWarnings({"unchecked"})
    protected void asyncInvoker(HostEntity host, T param, String command, Object data) {
        Objects.requireNonNull(host, "host cannot be null");
        Objects.requireNonNull(host.getUri(), "host uri cannot be null");
        Objects.requireNonNull(host.getClientId(), "clientId cannot be null");
        Objects.requireNonNull(host.getClientSecret(), "clientSecret cannot be null");
        String hostKey = RedisKeyUtil.getHostConnectionKey(host.getHostId());
        HostContext hostContext = (HostContext) this.redissonClient.getBucket(hostKey).get();
        if (hostContext == null) {
            throw new CodeException(ErrorCode.HOST_NOT_READY, "当前主机未就绪");
        }
        if (Objects.equals(this.applicationConfig.getCluster().getNodeUrl(), hostContext.getNodeUrl())) {
            TaskRequest taskRequest = TaskRequest.builder()
                    .command(command)
                    .data(GsonBuilderUtil.create().toJson(data))
                    .taskId(param.getTaskId()).build();
            String json = GsonBuilderUtil.create().toJson(taskRequest);
            log.info("转发任务到本地执行:task={} data={}", param.getTaskId(), json);
            HostClientManager.send(host.getHostId(), Constant.SocketCommand.AGENT_TASK_SUBMIT, json.getBytes(StandardCharsets.UTF_8));
            return;
        }
        String nodeUrl = hostContext.getNodeUrl();
        if (nodeUrl.endsWith("/")) {
            nodeUrl = nodeUrl.substring(0, nodeUrl.length() - 1);
        }
        String forwardUrl = nodeUrl + "/api/cluster/host/forward";
        this.executor.submit(() -> {
            RequestContextHolderUtil.initContext();
            Gson gson = GsonBuilderUtil.create();
            try {


                TaskRequest taskRequest = TaskRequest.builder()
                        .command(command)
                        .data(gson.toJson(data))
                        .taskId(param.getTaskId()).build();

                String nonce = String.valueOf(System.nanoTime());
                Map<String, Object> map = new HashMap<>(6);
                map.put("data", gson.toJson(taskRequest));
                map.put("hostId", host.getHostId());
                map.put("command", Constant.SocketCommand.AGENT_TASK_SUBMIT);
                map.put("nonce", nonce);
                map.put("timestamp", System.currentTimeMillis());

                String sign = SecurityUtil.signature(map, this.applicationConfig.getCluster().getToken());
                map.put("sign", sign);

                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
                map.forEach((k, v) -> requestMap.add(k, v != null ? v.toString() : ""));

                RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                        .post(URI.create(forwardUrl))
                        .headers(httpHeaders)
                        .body(requestMap);

                ResponseEntity<String> responseEntity = this.restTemplate.exchange(requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new CodeException(ErrorCode.SERVER_ERROR,
                            "Request failed with status: " + responseEntity.getStatusCode());
                }

                String response = responseEntity.getBody();
                ResultUtil<Object> resultUtil = gson.fromJson(response, this.getCallResultType());

                if (resultUtil == null) {
                    this.onSubmitFinishEvent(param.getTaskId(),
                            (V) ResultUtil.error(ErrorCode.SERVER_ERROR, "Invalid response: " + response));
                } else if (resultUtil.getCode() != ErrorCode.AGENT_TASK_ASYNC_WAIT) {
                    this.onSubmitFinishEvent(param.getTaskId(), (V) resultUtil);
                }
            } catch (Exception err) {
                this.onSubmitFinishEvent(param.getTaskId(), (V) ResultUtil.error(ErrorCode.SERVER_ERROR, "Request error: " + err.getMessage()));
            }finally {
                RequestContextHolderUtil.clearContext();
                NotifyContextHolderUtil.afterCompletion();
            }
        });
    }


    @SuppressWarnings("unchecked")
    @Override
    public void process(BaseOperateParam param) {
        this.operate((T) param);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onComplete(BaseOperateParam param, ResultUtil<?> resultUtil) {
        this.onFinish((T) param, (V) resultUtil);
    }


    protected void onSubmitFinishEvent(String taskId, V result) {
        this.taskService.submitTaskFinish(taskId, GsonBuilderUtil.create().toJson(result));
    }


    /**
     * 执行操作
     *
     * @param param
     */
    public abstract void operate(T param);

    /**
     * 执行结果回调
     *
     */
    public abstract void onFinish(T param, V resultUtil);

    /**
     * 获取处理类型
     *
     * @return
     */
    public abstract int getType();

    @Override
    public boolean requireLock() {
        return false;
    }

    @Override
    public String getLockKey(BaseOperateParam param) {
        return RedisKeyUtil.getGlobalLockKey();
    }

    protected Volume initVolume(StorageEntity storageEntity, VolumeEntity volumeEntity) {
        Map<String, Object> storageParam = GsonBuilderUtil.create().fromJson(storageEntity.getParam(), new TypeToken<Map<String, Object>>() {
        }.getType());
        return Volume.builder().storage(storageEntity.getName()).path(getVolumePath(storageEntity, volumeEntity.getName())).name(volumeEntity.getName()).type(volumeEntity.getType()).capacity(volumeEntity.getCapacity()).build();
    }

    protected Volume initVolume(StorageEntity storageEntity, TemplateVolumeEntity templateVolume) {
        Map<String, Object> storageParam = GsonBuilderUtil.create().fromJson(storageEntity.getParam(), new TypeToken<Map<String, Object>>() {
        }.getType());
        return Volume.builder().storage(storageEntity.getName()).path(getVolumePath(storageEntity, templateVolume.getName())).name(templateVolume.getName()).type(templateVolume.getType()).capacity(templateVolume.getCapacity()).build();
    }

    protected StorageCreateRequest buildStorageCreateRequest(StorageEntity storage, Map<String, Object> sysconfig) {
        String configKey;
        String secretKey = null;
        String secretValue = null;
        switch (storage.getType()) {
            case Constant.StorageType.CEPH_RBD:
                configKey = ConfigKey.STORAGE_CEPH_RBD_TPL;
                secretKey = ConfigKey.STORAGE_CEPH_RBD_SECRET_TPL;

                Map<String, Object> storageParam = GsonBuilderUtil.create().fromJson(storage.getParam(), new TypeToken<Map<String, Object>>() {
                }.getType());
                secretValue = storageParam.getOrDefault("secret", "").toString();
                break;
            case Constant.StorageType.GLUSTERFS:
                configKey = ConfigKey.STORAGE_GLUSTERFS_TPL;
                break;
            case Constant.StorageType.NFS:
                configKey = ConfigKey.STORAGE_NFS_TPL;
                break;
            case Constant.StorageType.LOCAL:
                configKey = ConfigKey.STORAGE_LOCAL_TPL;
                break;
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的存储池类型[" + storage.getType() + "]");
        }
        String storageTpl = (String) sysconfig.get(configKey);
        String storageXml = DomainUtil.buildStorageXml(storageTpl, sysconfig, storage);
        String secretXml = null;
        if (secretKey != null) {
            String secretTpl = (String) sysconfig.get(secretKey);
            secretXml = DomainUtil.buildStorageXml(secretTpl, sysconfig, storage);
        }
        return StorageCreateRequest.builder()
                .name(storage.getName())
                .type(storage.getType())
                .path(storage.getMountPath())
                .storageXml(storageXml)
                .secretXml(secretXml)
                .secretValue(secretValue)
                .build();
    }

    private String buildNetworkXml(NetworkEntity network, Map<String, Object> sysconfig) {
        Constant.NetworkBridgeType type = Constant.NetworkBridgeType.fromBridgeType(network.getBridgeType());
        String configKey;
        switch (type) {
            case BASIC:
                configKey = ConfigKey.NETWORK_DEFAULT_BRIDGE_TPL;
                break;
            case OPEN_SWITCH:
                configKey = ConfigKey.NETWORK_OVS_BRIDGE_TPL;
                break;
            default:
                throw new CodeException(ErrorCode.BASE_NETWORK_ERROR, "不支持的桥接方式");
        }
        String tpl = (String) sysconfig.get(configKey);
        return DomainUtil.buildNetworkXml(tpl, sysconfig, network);
    }

    protected BasicBridgeNetwork buildBasicNetworkRequest(NetworkEntity network, Map<String, Object> sysconfig) {
        return BasicBridgeNetwork.builder()
                .poolId(network.getPoolId())
                .xml(buildNetworkXml(network, sysconfig))
                .build();
    }

    protected VlanNetwork buildVlanCreateRequest(NetworkEntity basicNetworkEntity, NetworkEntity network, Map<String, Object> sysconfig) {

        BasicBridgeNetwork basicBridgeNetwork = buildBasicNetworkRequest(basicNetworkEntity, sysconfig);
        return VlanNetwork.builder()
                .poolId(network.getPoolId())
                .xml(buildNetworkXml(network, sysconfig))
                .basic(basicBridgeNetwork)
                .build();
    }


    protected VxLanNetwork buildVxLanCreateRequest(NetworkEntity basicNetworkEntity, NetworkEntity network, String localIp, String remoteIps, Map<String, Object> sysconfig) {

        BasicBridgeNetwork basicBridgeNetwork = buildBasicNetworkRequest(basicNetworkEntity, sysconfig);
        return VxLanNetwork.builder()
                .poolId(network.getPoolId())
                .xml(buildNetworkXml(network, sysconfig))
                .basic(basicBridgeNetwork)
                .vni(network.getNetworkId())
                .bridge(network.getBridge())
                .localIp(localIp)
                .remoteIps(remoteIps)
                .build();
    }
    protected Map<String, Object> loadGuestConfig(int hostId, int guestId) {
        List<ConfigQuery> queryList = new ArrayList<>();
        queryList.add(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).id(0).build());
        if (hostId > 0) {
            queryList.add(ConfigQuery.builder().type(Constant.ConfigType.HOST).id(hostId).build());
        }
        if (guestId > 0) {
            queryList.add(ConfigQuery.builder().type(Constant.ConfigType.GUEST).id(guestId).build());
        }
        return this.configService.loadSystemConfig(queryList);
    }

    protected Map<String, Object> loadVolumeConfig(int storageId, int volumeId) {
        List<ConfigQuery> queryList = new ArrayList<>();
        queryList.add(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).id(0).build());
        if (storageId > 0) {
            queryList.add(ConfigQuery.builder().type(Constant.ConfigType.STORAGE).id(storageId).build());
        }
        if (volumeId > 0) {
            queryList.add(ConfigQuery.builder().type(Constant.ConfigType.VOLUME).id(volumeId).build());
        }
        return this.configService.loadSystemConfig(queryList);
    }
}
