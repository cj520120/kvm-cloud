package cn.ruiyun.cloud.management.servcie;

import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.common.util.ResourceUtil;
import cn.ruiyun.cloud.management.config.ApplicationConfig;
import cn.ruiyun.cloud.management.data.dao.ConfigDao;
import cn.ruiyun.cloud.management.data.entity.ConfigEntity;
import cn.ruiyun.cloud.management.servcie.bean.ConfigInfo;
import cn.ruiyun.cloud.management.servcie.bean.ConfigQuery;
import cn.ruiyun.cloud.management.servcie.bean.DefaultConfigInfo;
import cn.ruiyun.cloud.management.servcie.convert.ConfigConvert;
import cn.ruiyun.cloud.management.servcie.convert.impl.FloatConvert;
import cn.ruiyun.cloud.management.servcie.convert.impl.IntegerConvert;
import cn.ruiyun.cloud.management.servcie.convert.impl.StringConvert;
import cn.ruiyun.cloud.management.servcie.convert.impl.VCpuTuneConvert;
import cn.ruiyun.cloud.management.util.ConfigKey;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ConfigService {
    private static final List<DefaultConfigInfo<?>> DEFAULT_CONFIG_LIST_CACHE = new ArrayList<>();
    private static final Map<String, DefaultConfigInfo<?>> DEFAULT_CONFIG_MAP_CACHE = new HashMap<>();
    @Autowired
    private ConfigDao configDao;

    public ConfigService(@Autowired ApplicationConfig applicationConfig) {
        // 系统通信地址
        initDefaultConfig(ConfigKey.DEFAULT_MANAGER_URI, applicationConfig.getManagerUri(), "System communication address", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        // Websocket通信基础地址(用于VNC和页面实时更新)
        initDefaultConfig(ConfigKey.DEFAULT_BASE_URI, "/", "Websocket communication base address (for VNC and real-time page updates)", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        // 是否启动网络组件(特殊情况下使用)
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_ENABLE, Constant.Enable.YES, "Whether to enable network components (for special cases)", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        // 系统组件网络驱动
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_NETWORK_DRIVER, Constant.NetworkDriver.VIRTIO, "System component network driver", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.NetworkDriver.VIRTIO, Constant.NetworkDriver.RTL8139, Constant.NetworkDriver.E1000), StringConvert.Default);
        // 系统组件Cpu
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_CPU, 1, "System component CPU", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 系统组件Cpu内存(MB)
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_MEMORY, 1024, "System component CPU memory (MB)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 系统组件Cpu Share
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_CPU_SHARE, 0, "System component CPU share", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 系统组件单个qma执行命令超时时间(分钟)
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_QMA_EXECUTE_TIMEOUT_MINUTES, 60, "Timeout for a single qma command executed by the system component (minutes)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 系统组件固件类型
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_BOOTSTRAP_TYPE, Constant.BootstrapType.BIOS_STR, "System component firmware type", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.BootstrapType.BIOS_STR, Constant.BootstrapType.UEFI_STR), StringConvert.Default);
        // 系统组件磁盘驱动
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_DISK_DRIVER, Constant.DiskDriveType.VIRTIO, "System component disk driver", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.DiskDriveType.SCSI, Constant.DiskDriveType.VIRTIO, Constant.DiskDriveType.SATA), StringConvert.Default);

        // 系统Cpu超分比例
        initDefaultConfig(ConfigKey.DEFAULT_OVER_CPU, 1.0f, "System CPU overcommit ratio", Constant.ConfigValueType.FLOAT, null, FloatConvert.Default);
        // 系统内存超分比例
        initDefaultConfig(ConfigKey.DEFAULT_OVER_MEMORY, 1.0f, "System memory overcommit ratio", Constant.ConfigValueType.FLOAT, null, FloatConvert.Default);


        // 系统组件连接检测超时时间(秒)
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_CONNET_TIMEOUT_SECONDS, 60, "System component connection check timeout (seconds)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 系统组件连接检测最大失败次数
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_CONNET_MAX_ERROR_COUNT, 3, "Maximum number of failed system component connection checks", Constant.ConfigValueType.INT, null, IntegerConvert.Default);


        // 组件默认用户名
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_USER_NAME, "kvm-user", "Default component username", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        // 组件默认密码
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_USER_PASSWORD, "Kvm@123456", "Default component password", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        // local-cloud加载模版配置
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_CLOUD_INIT_TPL, ResourceUtil.readUtf8Str("tpl/kvm/vm/cd/local/cloud.xml.json"), "local-cloud template loading configuration", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        // local-cloud默认配置路径
        initDefaultConfig(ConfigKey.SYSTEM_COMPONENT_CLOUD_INIT_PATH, "/var/kvm-cloud/", "local-cloud default configuration path", Constant.ConfigValueType.STRING, null, StringConvert.Default);


        // 申请主机时剩余CPU占比权重
        initDefaultConfig(ConfigKey.DEFAULT_ALLOCATE_HOST_CPU_WEIGHT, 30.0f, "Weight of remaining CPU percentage when allocating a host", Constant.ConfigValueType.FLOAT, null, FloatConvert.Default);
        // 申请主机时剩余内存占比权重(每GB)
        initDefaultConfig(ConfigKey.DEFAULT_ALLOCATE_HOST_MEMORY_WEIGHT, 20.0f, "Weight of remaining memory percentage when allocating a host (per GB)", Constant.ConfigValueType.FLOAT, null, FloatConvert.Default);
        // 申请磁盘时存储池占比权重(每TB)
        initDefaultConfig(ConfigKey.DEFAULT_ALLOCATE_STORAGE_WEIGHT, 1.0f, "Weight of storage pool percentage when allocating a disk (per TB)", Constant.ConfigValueType.FLOAT, null, FloatConvert.Default);
        // virtio-scsi控制器 queue 大小
        initDefaultConfig(ConfigKey.DEFAULT_VIRTIO_SCSI_QUEUE_NUMBER, 4, "virtio-scsi controller queue size", Constant.ConfigValueType.INT, null, IntegerConvert.Default);

        // 默认磁盘类型
        initDefaultConfig(ConfigKey.DEFAULT_DISK_TYPE, Constant.VolumeType.QCOW2, "Default disk type", Constant.ConfigValueType.SELECT, Arrays.asList(
                Constant.VolumeType.QCOW2,
                Constant.VolumeType.RAW,
                Constant.VolumeType.QCOW
        ), StringConvert.Default);
        // 默认模版磁盘类型
        initDefaultConfig(ConfigKey.DEFAULT_TEMPLATE_DISK_TYPE, Constant.VolumeType.QCOW2, "Default template disk type", Constant.ConfigValueType.SELECT, Arrays.asList(
                Constant.VolumeType.QCOW2,
                Constant.VolumeType.RAW,
                Constant.VolumeType.QCOW,
                Constant.VolumeType.VDI,
                Constant.VolumeType.VPC,
                Constant.VolumeType.VMDK
        ), StringConvert.Default);
        // 虚拟机关机最大等待时间，超过时间则直接进行销毁操作(分钟)
        initDefaultConfig(ConfigKey.DEFAULT_VM_STOP_MAX_EXPIRE_MINUTE, 10, "Maximum VM shutdown wait time; the VM is destroyed directly if this time is exceeded (minutes)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 执行删除操作延时保护周期
        initDefaultConfig(ConfigKey.DEFAULT_DESTROY_DELAY_MINUTE, 30, "Delayed protection period for delete operations", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 系统组件状态检测间隔(秒)
        initDefaultConfig(ConfigKey.DEFAULT_TASK_COMPONENT_CHECK_TIMEOUT_SECOND, 10, "System component status check interval (seconds)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 清理未管理系统组件虚拟机检测间隔(秒)
        initDefaultConfig(ConfigKey.DEFAULT_TASK_COMPONENT_GUEST_CLEAR_TIMEOUT_SECOND, 60, "Check interval for cleaning up unmanaged system component VMs (seconds)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 宿主机运行虚拟机状态检测间隔(秒)
        initDefaultConfig(ConfigKey.DEFAULT_TASK_HOST_GUEST_SYNC_CHECK_TIMEOUT_SECOND, 30, "Check interval for the status of running VMs on the host (seconds)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 宿主机状态检测间隔(秒)
        initDefaultConfig(ConfigKey.DEFAULT_TASK_HOST_CHECK_TIMEOUT_SECOND, 30, "Host status check interval (seconds)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 宿主机存储池检测间隔(秒)
        initDefaultConfig(ConfigKey.DEFAULT_TASK_STORAGE_CHECK_TIMEOUT_SECOND, 60, "Host storage pool check interval (seconds)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 存储池磁盘占用同步间隔(秒)
        initDefaultConfig(ConfigKey.DEFAULT_TASK_STORAGE_VOLUME_SYNC_TIMEOUT_SECOND, 600, "Storage pool disk usage synchronization interval (seconds)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 宿主机任务列表同步间隔(秒)，需要小于任务过期时间/2
        initDefaultConfig(ConfigKey.DEFAULT_TASK_HOST_TASK_SYNC_CHECK_TIMEOUT_SECOND, 30, "Host task list synchronization interval (seconds), must be less than half of the task expiration time", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 任务过期时间(秒)
        initDefaultConfig(ConfigKey.DEFAULT_TASK_EXPIRE_TIMEOUT_SECOND, 120, "Task expiration time (seconds)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 任务等待延期时间(秒)
        initDefaultConfig(ConfigKey.DEFAULT_TASK_WAIT_DELAY_SECOND, 10, "Task wait delay time (seconds)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);

        // 自动清理未关联的主机
        initDefaultConfig(ConfigKey.AUTO_DELETE_UNLINK_GUEST, Constant.Enable.YES, "Automatically clean up unassociated hosts", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);

        // cpu模式
        initDefaultConfig(ConfigKey.VM_CPU_MODEL, "host-passthrough", "CPU mode", Constant.ConfigValueType.SELECT, Arrays.asList("host-passthrough", "host-model", "custom"), StringConvert.Default);
        // Cpu绑定策略,例如[{"vcpu":0,"cpuset":0},{"vcpu":1,"cpuset":1}]
        initDefaultConfig(ConfigKey.VM_CPUTUNE_VCPUPIN_CONFIG, new ArrayList<>(0), "CPU binding policy, e.g. [{\"vcpu\":0,\"cpuset\":0},{\"vcpu\":1,\"cpuset\":1}]", Constant.ConfigValueType.MULTI_STRING, null, VCpuTuneConvert.Default);

        // numa内存分配模式
        initDefaultConfig(ConfigKey.VM_NUMA_MEMORY_MODEL, "", "NUMA memory allocation mode", Constant.ConfigValueType.SELECT, Arrays.asList("strict", "preferred", "interleave"), StringConvert.Default);
        // NUMA 节点编号（如 0、0-1、1,3）
        initDefaultConfig(ConfigKey.VM_NUMA_MEMORY_NODE, "", "NUMA node number (e.g. 0, 0-1, 1,3)", Constant.ConfigValueType.STRING, null, StringConvert.Default);

        // 是否支持内存气球技术(需要系统内核支持)
        initDefaultConfig(ConfigKey.VM_MEMORY_MEMBALLOON_ENABLE, "yes", "Whether to support memory ballooning (requires kernel support)", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        // 内存气球驱动方式
        initDefaultConfig(ConfigKey.VM_MEMORY_MEMBALLOON_MODEL, "virtio", "Memory balloon driver mode", Constant.ConfigValueType.SELECT, Arrays.asList("none", "virtio"), StringConvert.Default);
        // 内存气球回收周期(秒)
        initDefaultConfig(ConfigKey.VM_MEMORY_MEMBALLOON_PERIOD, 10, "Memory balloon reclamation period (seconds)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);


        // 是否启用大页内存
        initDefaultConfig(ConfigKey.VM_MEMORY_HUGE_PAGES_ENABLE, Constant.Enable.NO, "Whether to enable huge pages", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        // 大页内存设置值(GiB)
        initDefaultConfig(ConfigKey.VM_MEMORY_HUGE_PAGES_SIZE, 0, "Huge pages setting value (GiB)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 启用Cpu L3缓存(需要硬件支持)
        initDefaultConfig(ConfigKey.VM_CPU_CACHE_ENABLE, Constant.Enable.NO, "Enable CPU L3 cache (requires hardware support)", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        // 允许虚拟机内再运行虚拟化（需Intel/AMD支持)
        initDefaultConfig(ConfigKey.VM_CPU_VIRTUALIZATION_ENABLE, Constant.Enable.NO, "Allow nested virtualization in VMs (requires Intel/AMD support)", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        // 嵌套虚拟化名称(intel:vmx,amd:svm)
        initDefaultConfig(ConfigKey.VM_CPU_VIRTUALIZATION_NAME, "vmx", "Nested virtualization name (intel:vmx, amd:svm)", Constant.ConfigValueType.SELECT, Arrays.asList("vmx", "svm"), StringConvert.Default);

        // 虚拟机时钟配置
        initDefaultConfig(ConfigKey.VM_CLOCK_TYPE, "utc", "VM clock configuration", Constant.ConfigValueType.SELECT, Arrays.asList("utc", "localtime", "timezone", "variable"), StringConvert.Default);

        // VM机器名称
        initDefaultConfig(ConfigKey.VM_MACHINE_NAME, "", "vm machine name", Constant.ConfigValueType.STRING, null, StringConvert.Default);


        // nfs 存储池模版
        initDefaultConfig(ConfigKey.STORAGE_NFS_TPL, ResourceUtil.readUtf8Str("tpl/kvm/storage/nfs/storage.xml.json"), "NFS storage pool template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        // glusterfs 存储池模版
        initDefaultConfig(ConfigKey.STORAGE_GLUSTERFS_TPL, ResourceUtil.readUtf8Str("tpl/kvm/storage/glusterfs/storage.xml.json"), "GlusterFS storage pool template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        // ceph rbd 存储池密钥模版
        initDefaultConfig(ConfigKey.STORAGE_CEPH_RBD_SECRET_TPL, ResourceUtil.readUtf8Str("tpl/kvm/storage/ceph/secret.xml.json"), "Ceph RBD storage pool secret template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        // ceph rbd 存储池模版
        initDefaultConfig(ConfigKey.STORAGE_CEPH_RBD_TPL, ResourceUtil.readUtf8Str("tpl/kvm/storage/ceph/storage.xml.json"), "Ceph RBD storage pool template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        // local 存储池模版
        initDefaultConfig(ConfigKey.STORAGE_LOCAL_TPL, ResourceUtil.readUtf8Str("tpl/kvm/storage/local/storage.xml.json"), "local storage pool template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);

        // 基于系统桥接方式网络模版
        initDefaultConfig(ConfigKey.NETWORK_DEFAULT_BRIDGE_TPL, ResourceUtil.readUtf8Str("tpl/kvm/network/default/network.xml.json"), "Network template based on system bridge mode", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        // 基于OpenvSwitch桥接方式网络模版
        initDefaultConfig(ConfigKey.NETWORK_OVS_BRIDGE_TPL, ResourceUtil.readUtf8Str("tpl/kvm/network/ovs/network.xml.json"), "Network template based on OpenvSwitch bridge mode", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);


        // 磁盘读写缓冲(cache)模式
        initDefaultConfig(ConfigKey.VM_DISK_CACHE, "none", "Disk read/write cache mode", Constant.ConfigValueType.SELECT, Arrays.asList("writethrough", "writeback", "none", "unsafe", "directsync"), StringConvert.Default);
        // 磁盘读写AIO模式
        initDefaultConfig(ConfigKey.VM_DISK_AIO, "native", "Disk read/write AIO mode", Constant.ConfigValueType.SELECT, Arrays.asList("native", "thread"), StringConvert.Default);

        // 磁盘默认PCI总线层级(bus)
        initDefaultConfig(ConfigKey.VM_PCI_DISK_BUS, 0, "Default disk PCI bus level", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 磁盘PCI默认插槽(slot)
        initDefaultConfig(ConfigKey.VM_PCI_DISK_SLOT, 20, "Default disk PCI slot", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 磁盘PCI默认功能标识(function)
        initDefaultConfig(ConfigKey.VM_PCI_DISK_FUNCTION, 0, "Default disk PCI function", Constant.ConfigValueType.INT, null, IntegerConvert.Default);


        // 网卡默认PCI总线层级(bus)
        initDefaultConfig(ConfigKey.VM_PCI_NETWORK_BUS, 0, "Default NIC PCI bus level", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 网卡PCI默认插槽(slot)
        initDefaultConfig(ConfigKey.VM_PCI_NETWORK_SLOT, 10, "Default NIC PCI slot", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 网卡PCI默认功能标识(function)
        initDefaultConfig(ConfigKey.VM_PCI_NETWORK_FUNCTION, 0, "Default NIC PCI function", Constant.ConfigValueType.INT, null, IntegerConvert.Default);
        // 其他设备Xml配置
        initDefaultConfig(ConfigKey.VM_DEFAULT_DEVICE_TPL, "", "Other device XML configuration", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);


        // VM模版
        initDefaultConfig(ConfigKey.VM_DOMAIN_TPL, ResourceUtil.readUtf8Str("tpl/kvm/vm/vm.xml.json"), "VM template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);

        // vm nfs 磁盘模版
        initDefaultConfig(ConfigKey.VM_DISK_NFS_TPL, ResourceUtil.readUtf8Str("tpl/kvm/vm/disk/nfs/disk.xml.json"), "vm NFS disk template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        // vm glusterfs 磁盘模版
        initDefaultConfig(ConfigKey.VM_DISK_GLUSTERFS_TPL, ResourceUtil.readUtf8Str("tpl/kvm/vm/disk/glusterfs/disk.xml.json"), "vm GlusterFS disk template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        // vm ceph rbd 磁盘模版
        initDefaultConfig(ConfigKey.VM_DISK_CEPH_RBD_TPL, ResourceUtil.readUtf8Str("tpl/kvm/vm/disk/ceph/disk.xml.json"), "vm Ceph RBD disk template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        // vm local存储池磁盘模版
        initDefaultConfig(ConfigKey.VM_DISK_LOCAL_TPL, ResourceUtil.readUtf8Str("tpl/kvm/vm/disk/local/disk.xml.json"), "vm local storage pool disk template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        // vm local存储池磁盘模版
        initDefaultConfig(ConfigKey.VM_DISK_BLOCK_TPL, ResourceUtil.readUtf8Str("tpl/kvm/vm/disk/block/disk.xml.json"), "vm local storage pool disk template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        // vm local存储池磁盘模版
        initDefaultConfig(ConfigKey.VM_DISK_FILE_TPL, ResourceUtil.readUtf8Str("tpl/kvm/vm/disk/file/disk.xml.json"), "vm local storage pool disk template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);

        // vm nfs 光驱模版
        initDefaultConfig(ConfigKey.VM_CD_NFS_TPL, ResourceUtil.readUtf8Str("tpl/kvm/vm/cd/nfs/cd.xml.json"), "vm NFS CD-ROM template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        // vm glusterfs 光驱模版
        initDefaultConfig(ConfigKey.VM_CD_GLUSTERFS_TPL, ResourceUtil.readUtf8Str("tpl/kvm/vm/cd/glusterfs/cd.xml.json"), "vm GlusterFS CD-ROM template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        // vm ceph rbd 光驱模版
        initDefaultConfig(ConfigKey.VM_CD_CEPH_RBD_TPL, ResourceUtil.readUtf8Str("tpl/kvm/vm/cd/ceph/cd.xml.json"), "vm Ceph RBD CD-ROM template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);
        // vm local存储池光驱模版
        initDefaultConfig(ConfigKey.VM_CD_LOCAL_TPL, ResourceUtil.readUtf8Str("tpl/kvm/vm/cd/local/cd.xml.json"), "vm local storage pool CD-ROM template", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);

        // vm 基础网络网卡配置
        initDefaultConfig(ConfigKey.VM_INTERFACE_TPL, ResourceUtil.readUtf8Str("tpl/kvm/vm/interface/interface.xml.json"), "vm basic network interface configuration", Constant.ConfigValueType.MULTI_STRING, null, StringConvert.Default);


        // 登录使用的JWT 密码
        initDefaultConfig(ConfigKey.LOGIN_JWD_PASSWORD, "#$1fa)&*WS09", "JWT password used for login", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        // 登录使用的JWT ISSUser
        initDefaultConfig(ConfigKey.LOGIN_JWD_ISSUER, "CJ KVM Cloud Management", "JWT issuer used for login", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        // 登录token有效期(小时)
        initDefaultConfig(ConfigKey.LOGIN_JWT_EXPIRE_MINUTES, (int) TimeUnit.DAYS.toMinutes(1), "Login token validity period (hours)", Constant.ConfigValueType.INT, null, IntegerConvert.Default);


        // 是否启用Oauth2
        initDefaultConfig(ConfigKey.OAUTH2_ENABLE, Constant.Enable.NO, "Whether to enable OAuth2", Constant.ConfigValueType.SELECT, Arrays.asList(Constant.Enable.YES, Constant.Enable.NO), StringConvert.Default);
        // Oauth2 标题
        initDefaultConfig(ConfigKey.OAUTH2_TITLE, "Oauth2 Login", "Oauth2 Title", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        // Oauth2 客户端ID
        initDefaultConfig(ConfigKey.OAUTH2_CLIENT_ID, "", "Oauth2 Client Id", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        // Oauth2 客户端密钥
        initDefaultConfig(ConfigKey.OAUTH2_CLIENT_SECRET, "", "Oauth2 Client Secret", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        // Oauth2 授权地址
        initDefaultConfig(ConfigKey.OAUTH2_REQUEST_AUTH_URI, "", "Oauth2 Auth Uri", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        // Oauth2 获取Token地址
        initDefaultConfig(ConfigKey.OAUTH2_REQUEST_TOKEN_URI, "", "Oauth2 Request Token Uri", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        // Oauth2 获取用户信息地址
        initDefaultConfig(ConfigKey.OAUTH2_REQUEST_USER_URI, "", "Oauth2 Request User Uri", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        // Oauth2 回调地址
        initDefaultConfig(ConfigKey.OAUTH2_REDIRECT_URI, "", "Oauth2 Redirect Uri", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        // Oauth2 用户响应ID路径
        initDefaultConfig(ConfigKey.OAUTH2_USER_ID_PATH, "[\"userId\"]", "Oauth2 User Response ID Path", Constant.ConfigValueType.STRING, null, StringConvert.Default);

        // Oauth2 用户响应名称路径
        initDefaultConfig(ConfigKey.OAUTH2_USER_NAME_PATH, "[\"userName\"]", "Oauth2 User Response Name Path", Constant.ConfigValueType.STRING, null, StringConvert.Default);
        // Oauth2 用户响应权限路径
        initDefaultConfig(ConfigKey.OAUTH2_USER_AUTHORITIES_PATH, "[\"authorities\"]", "Oauth2 User Response Authorities Path", Constant.ConfigValueType.STRING, null, StringConvert.Default);

    }

    private static <T> void initDefaultConfig(String key, T value, String description, int valueType, Object valueOptions, ConfigConvert<T> convert) {
        DefaultConfigInfo<T> defaultConfig = DefaultConfigInfo.<T>builder().key(key).value(value).intern(true).description(description).valueType(valueType).valueOptions(valueOptions).convert(convert).build();
        DEFAULT_CONFIG_LIST_CACHE.add(defaultConfig);
        if (DEFAULT_CONFIG_MAP_CACHE.containsKey(key)) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "系统配置Key已存在:" + key);
        }
        DEFAULT_CONFIG_MAP_CACHE.put(key, defaultConfig);
    }

    public <T> T getConfig(String key) {
        return this.getConfig(Collections.singletonList(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).id(0).build()), key);
    }

    public <T> T getConfig(String key, T defaultValue) {
        T result = this.getConfig(key);
        return result != null ? result : defaultValue;
    }

    public <T> T getConfig(List<ConfigQuery> queryList, String key) {
        DefaultConfigInfo<T> defaultConfig = (DefaultConfigInfo<T>) DEFAULT_CONFIG_MAP_CACHE.get(key);
        T value = null;
        if (defaultConfig != null) {
            value = defaultConfig.getValue();
        }
        String queryStr = null;
        for (ConfigQuery query : queryList) {
            ConfigEntity findEntity = this.configDao.findByAllocateKey(key, query.getType(), query.getId());
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
            return value;
        }
    }

    public Map<String, Object> loadSystemConfig(List<ConfigQuery> queryList) {

        Map<String, Object> map = new HashMap<>();
        for (ConfigQuery query : queryList) {
            if (query.getType() == Constant.ConfigType.DEFAULT) {
                DEFAULT_CONFIG_LIST_CACHE.forEach(config -> map.put(config.getKey(), config.getValue()));
            }
            List<ConfigEntity> list = this.configDao.listByAllocate(query.getType(), query.getId());
            list.forEach(config -> {
                Object configValue = config.getConfigValue();
                DefaultConfigInfo<?> defaultConfig = DEFAULT_CONFIG_MAP_CACHE.get(config.getConfigKey());
                if (defaultConfig != null) {
                    configValue = defaultConfig.getConvert().convert(config.getConfigValue());
                }
                map.put(config.getConfigKey(), configValue);
            });
        }
        return map;
    }

    public List<ConfigInfo> listConfig(int allocateType, int allocateId) {
        List<ConfigInfo> list = new ArrayList<>();
        Map<String, ConfigInfo> map = new HashMap<>();
        if (allocateType == Constant.ConfigType.DEFAULT) {
            DEFAULT_CONFIG_LIST_CACHE.forEach(config -> {
                ConfigInfo model = new ConfigInfo();
                BeanUtils.copyProperties(config, model);
                model.setDefaultParam(true);
                list.add(model);
                map.put(model.getKey(), model);
            });
        }
        List<ConfigEntity> searchList = this.configDao.listByAllocate(allocateType, allocateId);
        searchList.forEach(config -> {
            ConfigInfo model = map.get(config.getConfigKey());
            if (model == null) {
                DefaultConfigInfo<?> defaultConfigInfo = DEFAULT_CONFIG_MAP_CACHE.get(config.getConfigKey());
                if (defaultConfigInfo != null) {
                    model = new ConfigInfo();
                    BeanUtils.copyProperties(defaultConfigInfo, model);
                    model.setIntern(false);
                    model.setDefaultParam(false);
                } else {
                    model = ConfigInfo.builder().id(config.getId()).defaultParam(false).key(config.getConfigKey()).allocateType(allocateType).allocateId(allocateId).value(config.getConfigValue()).valueType(Constant.ConfigValueType.MULTI_STRING).description("").valueOptions(null).build();
                }
                map.put(model.getKey(), model);
                list.add(model);
            }
            model.setId(config.getId());
            model.setValue(config.getConfigValue());
        });
        return list;
    }

    public ConfigInfo createConfig(String configKey, int allocateType, int allocateId, String configValue) {
        ConfigEntity entity = this.configDao.findByAllocateKey(configKey, allocateType, allocateId);
        if (entity != null) {
            throw new CodeException(ErrorCode.CONFIG_EXISTS_ERROR, "配置项已存在");
        }

        entity = ConfigEntity.builder().configKey(configKey).allocateType(allocateType).allocateId(allocateId).configValue(configValue).build();
        this.configDao.insert(entity);
        return this.initConfigInfo(entity);
    }

    public ConfigInfo updateConfig(String configKey, int allocateType, int allocateId, String configValue) {
        ConfigEntity entity = this.configDao.findByAllocateKey(configKey, allocateType, allocateId);
        boolean isFind = entity != null;
        if (!isFind) {
            DefaultConfigInfo<?> defaultConfig = DEFAULT_CONFIG_MAP_CACHE.get(configKey);
            if (defaultConfig == null) {
                throw new CodeException(ErrorCode.CONFIG_NOT_EXISTS_ERROR, "配置项不存在");
            } else {
                entity = ConfigEntity.builder().configKey(configKey).allocateType(allocateType).allocateId(allocateId).configValue(configValue).build();
            }
        }
        entity.setConfigValue(configValue);
        if (isFind) {
            this.configDao.update(entity);
        } else {
            this.configDao.insert(entity);
        }
        return this.initConfigInfo(entity);
    }

    public void deleteAllocateConfig(int allocateType, int allocateId) {
        this.configDao.deleteByAllocate(allocateType, allocateId);

    }

    public ConfigInfo deleteConfig(int id) {
        ConfigEntity entity = this.configDao.findById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.CONFIG_NOT_EXISTS_ERROR, "配置项不存在");
        }
        this.configDao.deleteById(id);
        DefaultConfigInfo<?> defaultConfig = DEFAULT_CONFIG_MAP_CACHE.get(entity.getConfigKey());
        ConfigInfo model = null;
        if (defaultConfig != null) {
            model = new ConfigInfo();
            BeanUtils.copyProperties(defaultConfig, model);
        }

        return model;
    }

    private ConfigInfo initConfigInfo(ConfigEntity entity) {
        DefaultConfigInfo<?> info = DEFAULT_CONFIG_MAP_CACHE.get(entity.getConfigKey());
        ConfigInfo model = new ConfigInfo();
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
