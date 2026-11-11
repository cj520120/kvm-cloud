package cn.ruiyun.cloud.management.operate.impl;

import cn.ruiyun.cloud.common.bean.*;
import cn.ruiyun.cloud.common.core.operate.BaseOperateParam;
import cn.ruiyun.cloud.common.core.operate.OperateService;
import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.gson.GsonBuilderUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.management.data.dao.*;
import cn.ruiyun.cloud.management.data.entity.*;
import cn.ruiyun.cloud.management.servcie.AllocateService;
import cn.ruiyun.cloud.management.servcie.CacheService;
import cn.ruiyun.cloud.management.servcie.ConfigService;
import cn.ruiyun.cloud.management.servcie.TaskService;
import cn.ruiyun.cloud.management.servcie.bean.ConfigQuery;
import cn.ruiyun.cloud.management.util.CacheKeyUtil;
import cn.ruiyun.cloud.management.util.ConfigKey;
import cn.ruiyun.cloud.management.util.DomainUtil;
import cn.ruiyun.cloud.management.websocket.listen.context.HostContext;
import cn.ruiyun.cloud.management.websocket.manager.HostClientManager;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

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
    protected CacheService cacheService;

    protected static Volume initVolume(StorageEntity storage, String volumeName, String type, long capacity) {


        switch (storage.getType()) {
            case Constant.StorageType.NFS:
            case Constant.StorageType.GLUSTERFS:
            case Constant.StorageType.LOCAL:
                return Volume.builder().storage(storage.getName()).path(storage.getMountPath() + "/" + volumeName).name(volumeName).type(type).capacity(capacity).build();

            case Constant.StorageType.CEPH_RBD:
                Map<String, Object> param = GsonBuilderUtil.create().fromJson(storage.getParam(), new TypeToken<Map<String, Object>>() {
                }.getType());
                String pool = param.get("pool").toString();
                String cephConfig = "[global]\nmon_host = " + param.get("uri") + "\n";
                String keyring = "[client." + param.get("username") + "]\nkey = " + param.get("secret").toString() + "\n";
                String configPath = "/tmp/" + UUID.randomUUID() + ".ceph.conf";
                String keyringPath = "/tmp/" + UUID.randomUUID() + ".ceph.client.admin.keyring";
                String path = String.format("rbd:%s/%s:id=%s:conf=%s:keyring=%s", pool, volumeName, param.get("username"), configPath, keyringPath);
                List<Config> configs = Arrays.asList(
                        Config.builder().path(configPath).content(cephConfig).build(),
                        Config.builder().path(keyringPath).content(keyring).build());
                return Volume.builder().storage(storage.getName()).path(path).name(volumeName).type(type).capacity(capacity).configs(configs).build();
            default:
                throw new CodeException(ErrorCode.STORAGE_NOT_SUPPORT, "未知的存储池类型:" + storage.getType());
        }

    }


    @Override
    public boolean supports(@NonNull Integer type) {
        return this.getType() == type;
    }

    @SuppressWarnings({"unchecked"})
    protected void asyncInvoker(HostEntity host, BaseOperateParam param, String command, Object data) {
        Objects.requireNonNull(host, "host cannot be null");
        Objects.requireNonNull(host.getUri(), "host uri cannot be null");
        Objects.requireNonNull(host.getClientId(), "clientId cannot be null");
        Objects.requireNonNull(host.getClientSecret(), "clientSecret cannot be null");
        String hostKey = CacheKeyUtil.getHostConnectionKey(host.getHostId());
        HostContext hostContext = this.cacheService.get(hostKey);
        if (hostContext == null) {
            throw new CodeException(ErrorCode.HOST_NOT_READY, "当前主机未就绪");
        }
        TaskRequest taskRequest = TaskRequest.builder()
                    .command(command)
                    .data(GsonBuilderUtil.create().toJson(data))
                    .taskId(param.getTaskId()).build();
        String json = GsonBuilderUtil.create().toJson(taskRequest);
        log.info("转发任务到本地执行:task={} data={}", param.getTaskId(), json);
        HostClientManager.send(host.getHostId(), Constant.SocketCommand.AGENT_TASK_SUBMIT, json.getBytes(StandardCharsets.UTF_8));
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
        return CacheKeyUtil.getGlobalLockKey();
    }

    protected Volume initVolume(StorageEntity storageEntity, VolumeEntity volumeEntity) {
        return initVolume(storageEntity, volumeEntity.getName(), volumeEntity.getType(), volumeEntity.getCapacity());
    }

    protected Volume initVolume(StorageEntity storageEntity, TemplateVolumeEntity templateVolume) {
        return initVolume(storageEntity, templateVolume.getName(), templateVolume.getType(), templateVolume.getCapacity());
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
            case LINUX_BRIDGE:
                configKey = ConfigKey.NETWORK_DEFAULT_BRIDGE_TPL;
                break;
            case OPEN_SWITCH_BRIDGE:
                configKey = ConfigKey.NETWORK_OVS_BRIDGE_TPL;
                break;
            default:
                throw new CodeException(ErrorCode.BASE_NETWORK_ERROR, "不支持的桥接方式");
        }
        String tpl = (String) sysconfig.get(configKey);
        return DomainUtil.buildNetworkXml(tpl, sysconfig, network);
    }

    protected BasicBridgeNetworkRequest buildBasicNetworkRequest(NetworkEntity network, Map<String, Object> sysconfig) {
        return BasicBridgeNetworkRequest.builder()
                .poolId(network.getPoolId())
                .xml(buildNetworkXml(network, sysconfig))
                .build();
    }

    protected VLanNetworkRequest buildVlanCreateRequest(NetworkEntity basicNetworkEntity, NetworkEntity network, Map<String, Object> sysconfig) {

        BasicBridgeNetworkRequest basicBridgeNetworkRequest = buildBasicNetworkRequest(basicNetworkEntity, sysconfig);
        return VLanNetworkRequest.builder()
                .poolId(network.getPoolId())
                .xml(buildNetworkXml(network, sysconfig))
                .basic(basicBridgeNetworkRequest)
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
