package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.AppUtils;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.data.mapper.*;
import cn.chenjun.cloud.management.operate.Operate;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.servcie.AllocateService;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.servcie.NotifyService;
import cn.chenjun.cloud.management.servcie.TaskService;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.util.DomainUtil;
import com.google.gson.reflect.TypeToken;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author chenjun
 */
public abstract class AbstractOperate<T extends BaseOperateParam, V extends ResultUtil> implements Operate {

    @Autowired
    protected GuestMapper guestMapper;
    @Autowired
    protected GuestDiskMapper guestDiskMapper;
    @Autowired
    protected GuestNetworkMapper guestNetworkMapper;
    @Autowired
    protected HostMapper hostMapper;
    @Autowired
    protected NetworkMapper networkMapper;
    @Autowired
    protected StorageMapper storageMapper;
    @Autowired
    protected TemplateMapper templateMapper;
    @Autowired
    protected TemplateVolumeMapper templateVolumeMapper;
    @Autowired
    protected VolumeMapper volumeMapper;
    @Autowired
    protected RedissonClient redissonClient;
    @Autowired
    protected SchemeMapper schemeMapper;
    @Autowired
    protected AllocateService allocateService;
    @Autowired
    protected NotifyService notifyService;
    @Autowired
    @Lazy
    protected TaskService taskService;
    @Autowired
    protected ConfigService configService;
    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected ScheduledExecutorService executor;

    @Override
    public boolean supports(@NonNull Integer type) {
        return this.getType() == type;
    }

    protected void asyncInvoker(HostEntity host, T param, String command, Object data) {
        this.executor.submit(() -> {
            try {
                TaskRequest taskRequest = TaskRequest.builder()
                        .command(command)
                        .data(GsonBuilderUtil.create().toJson(data))
                        .taskId(param.getTaskId()).build();
                String nonce = String.valueOf(System.nanoTime());
                Map<String, Object> map = new HashMap<>(6);
                map.put("data", GsonBuilderUtil.create().toJson(taskRequest));
                map.put("timestamp", System.currentTimeMillis());
                String sign = AppUtils.sign(map, host.getClientId(), host.getClientSecret(), nonce);
                map.put("sign", sign);
                String url = host.getUri();
                if (!url.endsWith("/")) {
                    url += "/";
                }
                url += "api/operate";
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
                map.forEach((k, v) -> requestMap.add(k, v.toString()));
                RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                        .post(URI.create(url))
                        .headers(httpHeaders)
                        .body(requestMap);
                ResponseEntity<String> responseEntity = this.restTemplate.exchange(requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "请求出错.status=" + responseEntity.getStatusCode());
                }
                String response = responseEntity.getBody();
                ResultUtil<Object> resultUtil = GsonBuilderUtil.create().fromJson(response, this.getCallResultType());
                if (resultUtil == null) {
                    ResultUtil<V> submitResult = ResultUtil.error(ErrorCode.SERVER_ERROR, "请求出错.response=" + response);
                    this.onSubmitFinishEvent(param.getTaskId(), (V) submitResult);
                } else if (resultUtil.getCode() != ErrorCode.AGENT_TASK_ASYNC_WAIT) {
                    this.onSubmitFinishEvent(param.getTaskId(), (V) resultUtil);
                }
            } catch (Exception err) {
                ResultUtil<V> submitResult = ResultUtil.error(ErrorCode.SERVER_ERROR, "数据请求出错");
                this.onSubmitFinishEvent(param.getTaskId(), (V) submitResult);
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
     * @param param
     * @param resultUtil
     */
    public abstract void onFinish(T param, V resultUtil);

    /**
     * 获取处理类型
     *
     * @return
     */
    public abstract int getType();

    protected Volume initVolume(StorageEntity storageEntity, VolumeEntity volumeEntity) {
        Map<String, Object> storageParam = GsonBuilderUtil.create().fromJson(storageEntity.getParam(), new TypeToken<Map<String, Object>>() {
        }.getType());
        Storage storage = Storage.builder()
                .name(storageEntity.getName())
                .type(storageEntity.getType())
                .param(storageParam)
                .mountPath(storageEntity.getMountPath())
                .build();
        return Volume.builder().storage(storage).name(volumeEntity.getName()).type(volumeEntity.getType()).capacity(volumeEntity.getCapacity()).build();
    }

    protected Volume initVolume(StorageEntity storageEntity, TemplateVolumeEntity volumeEntity) {
        Map<String, Object> storageParam = GsonBuilderUtil.create().fromJson(storageEntity.getParam(), new TypeToken<Map<String, Object>>() {
        }.getType());
        Storage storage = Storage.builder()
                .name(storageEntity.getName())
                .type(storageEntity.getType())
                .param(storageParam)
                .mountPath(storageEntity.getMountPath())
                .build();
        return Volume.builder().storage(storage).name(volumeEntity.getName()).type(volumeEntity.getType()).capacity(volumeEntity.getCapacity()).build();
    }

    protected StorageCreateRequest buildStorageCreateRequest(StorageEntity storage, Map<String, Object> sysconfig) {
        String configKey;
        String secretKey = null;
        String secretValue = null;
        switch (storage.getType()) {
            case Constant.StorageType.CEPH_RBD:
                configKey = cn.chenjun.cloud.management.util.Constant.ConfigKey.STORAGE_CEPH_RBD_TPL;
                secretKey = cn.chenjun.cloud.management.util.Constant.ConfigKey.STORAGE_CEPH_RBD_SECRET_TPL;

                Map<String, Object> storageParam = GsonBuilderUtil.create().fromJson(storage.getParam(), new TypeToken<Map<String, Object>>() {
                }.getType());
                secretValue = storageParam.getOrDefault("secret", "").toString();
                break;
            case Constant.StorageType.GLUSTERFS:
                configKey = cn.chenjun.cloud.management.util.Constant.ConfigKey.STORAGE_GLUSTERFS_TPL;
                break;
            case Constant.StorageType.NFS:
                configKey = cn.chenjun.cloud.management.util.Constant.ConfigKey.STORAGE_NFS_TPL;
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
        StorageCreateRequest request = StorageCreateRequest.builder()
                .name(storage.getName())
                .storageXml(storageXml)
                .secretXml(secretXml)
                .secretValue(secretValue)
                .build();
        return request;
    }

    private String buildNetworkXml(NetworkEntity network, Map<String, Object> sysconfig) {
        Constant.NetworkBridgeType type = Constant.NetworkBridgeType.fromBridgeType(network.getBridgeType());
        String configKey;
        switch (type) {
            case BASIC:
                configKey = cn.chenjun.cloud.management.util.Constant.ConfigKey.NETWORK_DEFAULT_BRIDGE_TPL;
                break;
            case OPEN_SWITCH:
                configKey = cn.chenjun.cloud.management.util.Constant.ConfigKey.NETWORK_OVS_BRIDGE_TPL;
                break;
            default:
                throw new CodeException(ErrorCode.BASE_NETWORK_ERROR, "不支持的桥接方式");
        }
        String tpl = (String) sysconfig.get(configKey);
        String xml = DomainUtil.buildNetworkXml(tpl, sysconfig, network);
        return xml;
    }

    protected BasicBridgeNetwork buildBasicNetworkRequest(NetworkEntity network, Map<String, Object> sysconfig) {
        BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder()
                .poolId(network.getPoolId())
                .xml(buildNetworkXml(network, sysconfig))
                .build();
        return basicBridgeNetwork;
    }

    protected VlanNetwork buildVlanCreateRequest(NetworkEntity basicNetworkEntity, NetworkEntity network, Map<String, Object> sysconfig) {

        BasicBridgeNetwork basicBridgeNetwork = buildBasicNetworkRequest(basicNetworkEntity, sysconfig);
        VlanNetwork vlan = VlanNetwork.builder()
                .poolId(network.getPoolId())
                .xml(buildNetworkXml(network, sysconfig))
                .basic(basicBridgeNetwork)
                .build();
        return vlan;
    }

    protected Map<String, Object> loadSystemConfig(int hostId, int guestId) {
        List<ConfigQuery> queryList = new ArrayList<>();
        queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.DEFAULT).id(0).build());
        if (hostId > 0) {
            queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.HOST).id(hostId).build());
        }
        if (guestId > 0) {
            queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.GUEST).id(guestId).build());
        }
        return this.configService.loadSystemConfig(queryList);
    }

}
