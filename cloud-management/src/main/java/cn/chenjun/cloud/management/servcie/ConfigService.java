package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.config.ApplicationConfig;
import cn.chenjun.cloud.management.data.entity.ConfigEntity;
import cn.chenjun.cloud.management.data.mapper.ConfigMapper;
import cn.chenjun.cloud.management.model.ConfigModel;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.servcie.bean.DefaultConfigInfo;
import cn.chenjun.cloud.management.servcie.convert.ConfigConvert;
import cn.chenjun.cloud.management.servcie.convert.bean.VCpuTune;
import cn.chenjun.cloud.management.servcie.convert.impl.FloatConvert;
import cn.chenjun.cloud.management.servcie.convert.impl.IntegerConvert;
import cn.chenjun.cloud.management.servcie.convert.impl.StringConvert;
import cn.chenjun.cloud.management.servcie.convert.impl.VCpuTuneConvert;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.Constant;
import cn.hutool.core.io.resource.ResourceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ConfigService {
    private static final List<DefaultConfigInfo> DEFAULT_CONFIG_LIST_CACHE = new ArrayList<>();
    private static final Map<String, DefaultConfigInfo> DEFAULT_CONFIG_MAP_CACHE = new HashMap<>();
    @Autowired
    private ConfigMapper mapper;

    public ConfigService(@Autowired ApplicationConfig applicationConfig) {
        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_MANAGER_URI,false, applicationConfig.getManagerUri(), "系统通信地址", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_ENABLE, true,Constant.Enable.YES, "是否启动网络组件(特殊情况下使用)", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_NETWORK_DRIVER, false,cn.chenjun.cloud.common.util.Constant.NetworkDriver.VIRTIO, "系统组件网络驱动", Constant.ConfigValueType.SELECT, Arrays.asList(cn.chenjun.cloud.common.util.Constant.NetworkDriver.VIRTIO, cn.chenjun.cloud.common.util.Constant.NetworkDriver.RTL8139, cn.chenjun.cloud.common.util.Constant.NetworkDriver.E1000), StringConvert.Default);
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_NETWORK_CHECK_ADDRESS, false,"8.8.8.8", "系统组件网络检测地址", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_CPU, false,1, "系统组件Cpu", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_MEMORY, false,1024, "系统组件Cpu内存(MB)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_CPU_SHARE, false,0, "系统组件Cpu Share", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_QMA_EXECUTE_TIMEOUT_MINUTES, false,60, "系统组件单个qma执行命令超时时间(分钟)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_QMA_CHECK_TIMEOUT_MINUTES,false, 10, "系统组件qma启动超时时间(分钟)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_PIP_INSTALL_SOURCE, false,"", "系统组件pip加速源", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_YUM_INSTALL_SOURCE,false, "", "系统组件yum加速源", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_OVER_CPU, false,1.0f, "系统Cpu超分比例", Constant.ConfigValueType.FLOAT, null, FloatConvert.Default);
        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_OVER_MEMORY, false,1.0f, "系统内存超分比例", Constant.ConfigValueType.FLOAT, null, FloatConvert.Default);

        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_ENABLE_VIRTIO_SCSI, false,Constant.Enable.YES, "是否使用virtio-scsi", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_VIRTIO_SCSI_QUEUE_NUMBER, false,4, "virtio-scsi queue 大小", Constant.ConfigValueType.INT, null, IntegerConvert.Default);

        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_DISK_TYPE,false, cn.chenjun.cloud.common.util.Constant.VolumeType.QCOW2, "默认磁盘类型", Constant.ConfigValueType.SELECT, Arrays.asList(
                cn.chenjun.cloud.common.util.Constant.VolumeType.QCOW2,
                cn.chenjun.cloud.common.util.Constant.VolumeType.RAW,
                cn.chenjun.cloud.common.util.Constant.VolumeType.QCOW
        ), StringConvert.Default);
        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_TEMPLATE_DISK_TYPE,false, cn.chenjun.cloud.common.util.Constant.VolumeType.QCOW2, "默认模版磁盘驱动", Constant.ConfigValueType.SELECT, Arrays.asList(
                cn.chenjun.cloud.common.util.Constant.VolumeType.QCOW2,
                cn.chenjun.cloud.common.util.Constant.VolumeType.RAW,
                cn.chenjun.cloud.common.util.Constant.VolumeType.QCOW,
                cn.chenjun.cloud.common.util.Constant.VolumeType.VDI,
                cn.chenjun.cloud.common.util.Constant.VolumeType.VPC,
                cn.chenjun.cloud.common.util.Constant.VolumeType.VMDK
        ), StringConvert.Default);
        initDefaultConfig(ConfigKey.DEFAULT_VM_STOP_MAX_EXPIRE_MINUTE, false,10, "虚拟机关机最大等待时间，超过时间则直接进行销毁操作(分钟)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_DESTROY_DELAY_MINUTE, false,10, "执行删除操作延时保护周期", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_TASK_CLEAR_COMPONENT_TIMEOUT_SECOND,false, 60, "清理未关联系统组件间隔(秒)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_TASK_COMPONENT_CHECK_TIMEOUT_SECOND,false, 10, "系统组件状态检测间隔(秒)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_TASK_HOST_GUEST_SYNC_CHECK_TIMEOUT_SECOND, false,30, "宿主机运行虚拟机状态检测间隔(秒)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_TASK_HOST_CHECK_TIMEOUT_SECOND, false,30, "宿主机状态检测间隔(秒)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_TASK_STORAGE_CHECK_TIMEOUT_SECOND, false,60, "宿主机存储池检测间隔(秒)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_TASK_STORAGE_VOLUME_SYNC_TIMEOUT_SECOND, false,600, "存储池磁盘占用同步间隔(秒)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_TASK_HOST_TASK_SYNC_CHECK_TIMEOUT_SECOND,false, 30, "宿主机任务列表同步间隔(秒)，需要小于任务过期时间/2", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.DEFAULT_CLUSTER_TASK_EXPIRE_TIMEOUT_SECOND, false,120, "任务过期时间(秒)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);

        initDefaultConfig(ConfigKey.VM_CPUTUNE_VCPUPIN_ENABLE, false,"no", "是否启用Cpu绑定策略功能（需要配置在虚拟机配置中,请在虚拟机绑定主机的情况下使用）", Constant.ConfigValueType.STRING, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_CPUTUNE_VCPUPIN_CONFIG, false,new ArrayList<VCpuTune>(0), "Cpu绑定策略,例如[{\"vcpu\":0,\"cpuset\":0},{\"vcpu\":1,\"cpuset\":1}]", Constant.ConfigValueType.MULTI_STRING, null, VCpuTuneConvert.Default);

        initDefaultConfig(ConfigKey.VM_BIND_HOST, true,0, "虚拟机绑定主机ID(只支持配置在虚拟机配置中)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.VM_NUMA_MEMORY_ENABLE, false,"no", "是否启用numa(请在虚拟机绑定主机的情况下使用，并配置在单独的虚拟机配置中)", Constant.ConfigValueType.STRING, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_NUMA_MEMORY_MODEL, false,"strict", "numa内存分配模式", Constant.ConfigValueType.STRING, Arrays.asList("strict","preferred","interleave"), StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_NUMA_MEMORY_NODE, false,"0", "NUMA 节点编号（如 0、0-1、1,3）", Constant.ConfigValueType.STRING, null, StringConvert.Default);

        initDefaultConfig(ConfigKey.VM_MEMORY_MEMBALLOON_ENABLE, false,"yes", "是否支持内存气球技术(需要系统内核支持)", Constant.ConfigValueType.STRING, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_MEMORY_MEMBALLOON_MODEL, false,"virtio", "内存气球驱动方式", Constant.ConfigValueType.STRING, Arrays.asList("none","virtio"), StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_MEMORY_MEMBALLOON_PERIOD, false,10, "内存气球回收周期(秒)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);


        initDefaultConfig(ConfigKey.VM_MEMORY_HUGE_PAGES_ENABLE, false,Constant.Enable.NO, "是否启用大页内存", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_MEMORY_HUGE_PAGES_SIZE, false,0, "大页内存设置值(GiB)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.VM_CPU_CACHE_ENABLE,false, Constant.Enable.NO, "启用Cpu L3缓存(需要硬件支持)", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_CPU_VIRTUALIZATION_ENABLE,false, Constant.Enable.NO, "允许虚拟机内再运行虚拟化（需Intel/AMD支持)", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_CPU_VIRTUALIZATION_NAME, false,"vmx", "嵌套虚拟化名称(intel:vmx,amd:svm)", Constant.ConfigValueType.SELECT, Arrays.asList("vmx", "svm"), StringConvert.Default);

        initDefaultConfig(ConfigKey.VM_CLOCK_TYPE, false,"utc", "虚拟机时钟配置", Constant.ConfigValueType.SELECT, Arrays.asList("utc", "localtime", "timezone", "variable"), StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_CD_BUS,false, "ide", "默认光驱驱动方式", Constant.ConfigValueType.SELECT, Arrays.asList("ide", "sata", "scsi"), StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_DEFAULT_UEFI_LOADER_TYPE, false,"pflash", "Uefi Loader Type", Constant.ConfigValueType.SELECT, Arrays.asList("pflash", "rom"), StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_DEFAULT_UEFI_LOADER_PATH,false, "/usr/share/edk2.git/ovmf-x64/OVMF_CODE-pure-efi.fd", "Uefi Loader Path", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_MACHINE_ARCH, false,"x86_64", "vm machine arch", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_MACHINE_NAME, false,"", "vm machine name", Constant.ConfigValueType.STRING, null, StringConvert.Default);

        initDefaultConfig(ConfigKey.STORAGE_LOCAL_ENABLE,true, Constant.Enable.NO, "是否启用本地存储(实验阶段,不支持系统组件创建和启动，系统必须至少包含一个用于磁盘存储的共享存储池)", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        initDefaultConfig(ConfigKey.STORAGE_LOCAL_PATH,true, "/data", "本地存储路径，需要在主机节点提前创建", Constant.ConfigValueType.STRING, null, StringConvert.Default);

        initDefaultConfig(ConfigKey.STORAGE_NFS_TPL, false,ResourceUtil.readUtf8Str("tpl/kvm/storage/nfs/storage.xml"), "nfs 存储池模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.STORAGE_GLUSTERFS_TPL,false, ResourceUtil.readUtf8Str("tpl/kvm/storage/glusterfs/storage.xml"), "glusterfs 存储池模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.STORAGE_CEPH_RBD_SECRET_TPL,false, ResourceUtil.readUtf8Str("tpl/kvm/storage/ceph/secret.xml"), "ceph rbd 存储池密钥模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.STORAGE_CEPH_RBD_TPL,false, ResourceUtil.readUtf8Str("tpl/kvm/storage/ceph/storage.xml"), "ceph rbd 存储池模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.STORAGE_LOCAL_TPL, true,ResourceUtil.readUtf8Str("tpl/kvm/storage/local/storage.xml"), "local 存储池模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);

        initDefaultConfig(ConfigKey.NETWORK_DEFAULT_BRIDGE_TPL, false,ResourceUtil.readUtf8Str("tpl/kvm/network/default/network.xml"), "基于系统桥接方式网络模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.NETWORK_OVS_BRIDGE_TPL, false,ResourceUtil.readUtf8Str("tpl/kvm/network/ovs/network.xml"), "基于OpenvSwitch桥接方式网络模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);

        initDefaultConfig(ConfigKey.VM_PCI_DISK_BUS, false,0, "磁盘P默认CI总线层级(bus)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.VM_PCI_DISK_SLOT, false,20, "磁盘PCI默认插槽(slot)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.VM_PCI_DISK_FUNCTION, false,0, "磁盘PCI默认功能标识(function)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);


        initDefaultConfig(ConfigKey.VM_PCI_NETWORK_BUS, false,0, "网卡P默认CI总线层级(bus)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.VM_PCI_NETWORK_SLOT, false,10, "网卡PCI默认插槽(slot)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.VM_PCI_NETWORK_FUNCTION,false, 0, "网卡PCI默认功能标识(function)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        initDefaultConfig(ConfigKey.VM_DEFAULT_DEVICE_TPL,false, "", "其他设备Xml配置", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);


        initDefaultConfig(ConfigKey.VM_DOMAIN_TPL,false, ResourceUtil.readUtf8Str("tpl/kvm/vm/vm.xml"), "VM模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);

        initDefaultConfig(ConfigKey.VM_DISK_NFS_TPL,false, ResourceUtil.readUtf8Str("tpl/kvm/vm/disk/nfs/disk.xml"), "vm nfs 磁盘模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_DISK_GLUSTERFS_TPL,false, ResourceUtil.readUtf8Str("tpl/kvm/vm/disk/glusterfs/disk.xml"), "vm glusterfs 磁盘模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_DISK_CEPH_RBD_TPL,false, ResourceUtil.readUtf8Str("tpl/kvm/vm/disk/ceph/disk.xml"), "vm ceph rbd 磁盘模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_DISK_LOCAL_TPL, true,ResourceUtil.readUtf8Str("tpl/kvm/vm/disk/local/disk.xml"), "vm local存储池磁盘模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);

        initDefaultConfig(ConfigKey.VM_CD_NFS_TPL,false, ResourceUtil.readUtf8Str("tpl/kvm/vm/cd/nfs/cd.xml"), "vm nfs 光驱模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_CD_GLUSTERFS_TPL,false, ResourceUtil.readUtf8Str("tpl/kvm/vm/cd/glusterfs/cd.xml"), "vm glusterfs 光驱模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_CD_CEPH_RBD_TPL, false,ResourceUtil.readUtf8Str("tpl/kvm/vm/cd/ceph/cd.xml"), "vm ceph rbd 光驱模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.VM_CD_LOCAL_TPL,true, ResourceUtil.readUtf8Str("tpl/kvm/vm/cd/local/cd.xml"), "vm local存储池光驱模版", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);

        initDefaultConfig(ConfigKey.VM_INTERFACE_TPL,false, ResourceUtil.readUtf8Str("tpl/kvm/vm/interface/interface.xml"), "vm 基础网络网卡配置", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);


        initDefaultConfig(ConfigKey.LOGIN_JWD_PASSWORD,false, "#$1fa)&*WS09", "登录使用的JWT 密码", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        initDefaultConfig(ConfigKey.LOGIN_JWD_ISSUER,false, "CJ Cloud Management", "登录使用的JWT ISSUser", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        initDefaultConfig(ConfigKey.LOGIN_JWT_EXPIRE_MINUTES,false, (int) TimeUnit.DAYS.toMinutes(1), "登录token有效期(小时)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);


        initDefaultConfig(ConfigKey.OAUTH2_ENABLE, false,Constant.Enable.NO, "是否启用Oauth2", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        initDefaultConfig(ConfigKey.OAUTH2_TITLE, false,"Oauth2 Login", "Oauth2 Title", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.OAUTH2_CLIENT_ID, false,"", "Oauth2 Client Id", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.OAUTH2_CLIENT_SECRET, false,"", "Oauth2 Client Secret", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.OAUTH2_REQUEST_AUTH_URI,false, "", "Oauth2 Auth Uri", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.OAUTH2_REQUEST_TOKEN_URI,false, "", "Oauth2 Request Token Uri", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.OAUTH2_REQUEST_USER_URI, false,"", "Oauth2 Request User Uri", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.OAUTH2_REDIRECT_URI, false,"", "Oauth2 Redirect Uri", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.OAUTH2_USER_ID_PATH, false,"[\"userId\"]", "Oauth2 User Response ID Path", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        initDefaultConfig(ConfigKey.OAUTH2_USER_AUTHORITIES_PATH, false,"[\"authorities\"]", "Oauth2 User Response Authorities Path", Constant.ConfigValueType.STRING, null, StringConvert.Default);

    }

    private static <T> void initDefaultConfig(String key, boolean intern,T value, String description, int valueType, Object valueOptions, ConfigConvert<T> convert) {
        DefaultConfigInfo<T> defaultConfig = DefaultConfigInfo.<T>builder().key(key).intern(intern).value(value).description(description).valueType(valueType).valueOptions(valueOptions).convert(convert).build();
        DEFAULT_CONFIG_LIST_CACHE.add(defaultConfig);
        if (DEFAULT_CONFIG_MAP_CACHE.containsKey(key)) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "系统配置Key已存在:" + key);
        }
        DEFAULT_CONFIG_MAP_CACHE.put(key, defaultConfig);
    }

    public <T> T getConfig(String key) {
        return this.getConfig(Collections.singletonList(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).id(0).build()), key);
    }

    public <T> T getConfig(List<ConfigQuery> queryList, String key) {
        DefaultConfigInfo<T> defaultConfig = DEFAULT_CONFIG_MAP_CACHE.get(key);
        T value = null;
        if (defaultConfig != null) {
            value = defaultConfig.getValue();
        }
        String queryStr = null;
        for (ConfigQuery query : queryList) {
            ConfigEntity findEntity = this.mapper.selectOne(new QueryWrapper<ConfigEntity>().eq(ConfigEntity.CONFIG_KEY, key).eq(ConfigEntity.CONFIG_ALLOCATE_TYPE, query.getType()).eq(ConfigEntity.CONFIG_ALLOCATE_ID, query.getId()));
            if (findEntity != null) {
                queryStr = findEntity.getConfigValue();
            }
        }
        if (queryStr != null) {
            if (defaultConfig != null) {
                return defaultConfig.getConvert().convert(queryStr);
            }
            return (T) queryStr;
        } else {
            if (value == null) {
                new CodeException(ErrorCode.SERVER_ERROR, "未知的配置项:" + key);
            }
            return value;
        }
    }

    public Map<String, Object> loadSystemConfig(List<ConfigQuery> queryList) {

        Map<String, Object> map = new HashMap<>();
        for (ConfigQuery query : queryList) {
            if (query.getType() == Constant.ConfigType.DEFAULT) {
                DEFAULT_CONFIG_LIST_CACHE.stream().forEach(config -> map.put(config.getKey(), config.getValue()));
            }
            List<ConfigEntity> list = this.mapper.selectList(new QueryWrapper<ConfigEntity>().eq(ConfigEntity.CONFIG_ALLOCATE_TYPE, query.getType()).eq(ConfigEntity.CONFIG_ALLOCATE_ID, query.getId()));
            list.stream().forEach(config -> {
                Object configValue = config.getConfigValue();
                DefaultConfigInfo defaultConfig = DEFAULT_CONFIG_MAP_CACHE.get(config.getConfigKey());
                if (defaultConfig != null) {
                    configValue = defaultConfig.getConvert().convert(config.getConfigValue());
                }
                map.put(config.getConfigKey(), configValue);
            });
        }
        return map;
    }


    public ResultUtil<List<ConfigModel>> listConfig(int allocateType, int allocateId) {
        List<ConfigModel> list = new ArrayList<>();
        Map<String, ConfigModel> map = new HashMap<>();
        if (allocateType == Constant.ConfigType.DEFAULT) {
            DEFAULT_CONFIG_LIST_CACHE.stream().forEach(config -> {
                ConfigModel model = new ConfigModel();
                BeanUtils.copyProperties(config, model);
                model.setDefaultParam(true);
                list.add(model);
                map.put(model.getKey(), model);
            });
        }
        List<ConfigEntity> searchList = this.mapper.selectList(new QueryWrapper<ConfigEntity>().eq(ConfigEntity.CONFIG_ALLOCATE_TYPE, allocateType).eq(ConfigEntity.CONFIG_ALLOCATE_ID, allocateId));
        searchList.stream().forEach(config -> {
            ConfigModel model = map.get(config.getConfigKey());
            if(model==null){
                model=new ConfigModel();
                DefaultConfigInfo defaultConfigInfo=DEFAULT_CONFIG_MAP_CACHE.get(config.getConfigKey());
                if(defaultConfigInfo!=null) {
                    BeanUtils.copyProperties(defaultConfigInfo, model);
                    model.setIntern(false);
                    model.setDefaultParam(false);
                    list.add(model);
                    map.put(model.getKey(), model);
                }else {
                    model = ConfigModel.builder().id(config.getId()).defaultParam(false).key(config.getConfigKey()).allocateType(allocateType).allocateId(allocateId).value(config.getConfigValue()).valueType(Constant.ConfigValueType.MULTI_STRING).description("").valueOptions(null).build();
                }
            }
            model.setId(config.getId());
            model.setValue(config.getConfigValue());
        });
        return ResultUtil.success(list);
    }

    public ResultUtil<ConfigModel> createConfig(String configKey, int allocateType, int allocateId, String configValue) {
        ConfigEntity entity = this.mapper.selectOne(new QueryWrapper<ConfigEntity>().eq(ConfigEntity.CONFIG_KEY, configKey).eq(ConfigEntity.CONFIG_ALLOCATE_TYPE, allocateType).eq(ConfigEntity.CONFIG_ALLOCATE_ID, allocateId));
        if (entity != null) {
            throw new CodeException(ErrorCode.CONFIG_EXISTS_ERROR, "配置项已存在");
        }

        entity = ConfigEntity.builder().configKey(configKey).allocateType(allocateType).allocateId(allocateId).configValue(configValue).build();
        this.mapper.insert(entity);
        return ResultUtil.success(this.initConfigModel(entity));
    }

    public ResultUtil<ConfigModel> updateConfig(String configKey, int allocateType, int allocateId, String configValue) {
        QueryWrapper<ConfigEntity> queryWrapper = new QueryWrapper<ConfigEntity>().eq(ConfigEntity.CONFIG_KEY, configKey).eq(ConfigEntity.CONFIG_ALLOCATE_TYPE, allocateType).eq(ConfigEntity.CONFIG_ALLOCATE_ID, allocateId);
        ConfigEntity entity = this.mapper.selectOne(queryWrapper);
        boolean isFind = entity != null;
        if (!isFind) {
            DefaultConfigInfo defaultConfig = DEFAULT_CONFIG_MAP_CACHE.get(configKey);
            if (defaultConfig == null) {
                throw new CodeException(ErrorCode.CONFIG_NOT_EXISTS_ERROR, "配置项不存在");
            } else {
                entity = ConfigEntity.builder().configKey(configKey).allocateType(allocateType).allocateId(allocateId).configValue(configValue).build();
            }
        }
        entity.setConfigValue(configValue);
        if (isFind) {
            this.mapper.updateById(entity);
        } else {
            this.mapper.insert(entity);
        }
        return ResultUtil.success(this.initConfigModel(entity));
    }

    public void deleteAllocateConfig(int allocateType, int allocateId) {
        QueryWrapper<ConfigEntity> queryWrapper = new QueryWrapper<ConfigEntity>().eq(ConfigEntity.CONFIG_ALLOCATE_TYPE, allocateType).eq(ConfigEntity.CONFIG_ALLOCATE_ID, allocateId);
        this.mapper.delete(queryWrapper);

    }

    public ResultUtil<ConfigModel> deleteConfig(int id) {
        ConfigEntity entity = this.mapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.CONFIG_NOT_EXISTS_ERROR, "配置项不存在");
        }
        this.mapper.deleteById(id);
        DefaultConfigInfo defaultConfig = DEFAULT_CONFIG_MAP_CACHE.get(entity.getConfigKey());
        ConfigModel model = null;
        if (defaultConfig != null) {
            model = new ConfigModel();
            BeanUtils.copyProperties(defaultConfig, model);
        }

        return ResultUtil.success(model);
    }

    private ConfigModel initConfigModel(ConfigEntity entity) {
        DefaultConfigInfo info = DEFAULT_CONFIG_MAP_CACHE.get(entity.getConfigKey());
        ConfigModel model = new ConfigModel();
        if (info != null) {
            BeanUtils.copyProperties(info, model);
            model.setDefaultParam(true);
        } else {
            model.setId(entity.getId());
            model.setKey(entity.getConfigKey());
            model.setAllocateId(entity.getAllocateId());
            model.setAllocateType(entity.getAllocateType());
            model.setDescription("");
            model.setValueOptions(null);
            model.setDefaultParam(false);
            model.setIntern(false);
            model.setValueType(Constant.ConfigValueType.MULTI_STRING);
        }
        model.setId(entity.getId());
        model.setValue(entity.getConfigValue());
        return model;
    }
}
