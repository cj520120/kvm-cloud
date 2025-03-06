package cn.chenjun.cloud.management.util;

public class ConfigKey {
    public static final String SYSTEM_COMPONENT_NETWORK_DRIVER = "system.component.network.driver";
    public static final String SYSTEM_COMPONENT_NETWORK_CHECK_ADDRESS = "system.component.network.check.address";
    public static final String SYSTEM_COMPONENT_CPU = "system.component.cpu.number";
    public static final String SYSTEM_COMPONENT_MEMORY = "system.component.memory";
    public static final String SYSTEM_COMPONENT_ENABLE = "system.component.enable";
    public static final String SYSTEM_COMPONENT_CPU_SHARE = "system.component.cpu.share";
    public static final String SYSTEM_COMPONENT_QMA_EXECUTE_TIMEOUT_MINUTES = "system.component.qma.execute.timeout.minutes";
    public static final String SYSTEM_COMPONENT_QMA_CHECK_TIMEOUT_MINUTES = "system.component.qma.check.timeout.minutes";
    public static final String SYSTEM_COMPONENT_PIP_INSTALL_SOURCE = "system.component.pip.source";
    public static final String SYSTEM_COMPONENT_YUM_INSTALL_SOURCE = "system.component.yum.repo";
    public static final String DEFAULT_CLUSTER_OVER_CPU = "default.cluster.over.cpu";
    public static final String DEFAULT_CLUSTER_DISK_TYPE = "default.cluster.disk.type";
    public static final String DEFAULT_CLUSTER_TEMPLATE_DISK_TYPE = "default.cluster.template.disk.type";
    public static final String DEFAULT_CLUSTER_OVER_MEMORY = "default.cluster.over.memory";
    public static final String DEFAULT_CLUSTER_MANAGER_URI = "default.cluster.manager.uri";

    public static final String DEFAULT_CLUSTER_DESTROY_DELAY_MINUTE = "default.cluster.destroy.delay.timeout.minutes";
    public static final String DEFAULT_VM_STOP_MAX_EXPIRE_MINUTE = "default.vm.stop.max.wait.timeout.minutes";

    public static final String DEFAULT_CLUSTER_TASK_CLEAR_COMPONENT_TIMEOUT_SECOND = "default.cluster.component.clear.timeout.second";
    public static final String DEFAULT_CLUSTER_TASK_COMPONENT_CHECK_TIMEOUT_SECOND = "default_cluster_component.check.timeout.second";
    public static final String DEFAULT_CLUSTER_TASK_HOST_GUEST_SYNC_CHECK_TIMEOUT_SECOND = "default.cluster.host.guest.sync.check.timeout.second";
    public static final String DEFAULT_CLUSTER_TASK_HOST_TASK_SYNC_CHECK_TIMEOUT_SECOND = "default.cluster.host.task.sync.check.timeout.second";
    public static final String DEFAULT_CLUSTER_TASK_HOST_CHECK_TIMEOUT_SECOND = "default.cluster.host.check.timeout.second";
    public static final String DEFAULT_CLUSTER_TASK_STORAGE_CHECK_TIMEOUT_SECOND = "default.cluster.storage.check.timeout.second";
    public static final String DEFAULT_CLUSTER_TASK_STORAGE_VOLUME_SYNC_TIMEOUT_SECOND = "default.cluster.storage.volume.sync.timeout.second";
    public static final String DEFAULT_CLUSTER_TASK_EXPIRE_TIMEOUT_SECOND = "default.cluster.task.expire.timeout.second";

    public static final String VM_BIND_HOST = "vm.bind.host";
    public static final String VM_NUMA_MEMORY_ENABLE = "vm.numa.memory.enable";
    public static final String VM_NUMA_MEMORY_MODEL = "vm.numa.memory.model";
    public static final String VM_NUMA_MEMORY_NODE = "vm.numa.memory.node";
    public static final String VM_CPUTUNE_VCPUPIN_ENABLE = "vm.cputune.vcpupin.enable";
    public static final String VM_CPUTUNE_VCPUPIN_CONFIG = "vm.cputune.vcpupin.config";
    public static final String VM_MEMORY_MEMBALLOON_ENABLE = "vm.memory.memballoon.enable";
    public static final String VM_MEMORY_MEMBALLOON_PERIOD = "vm.memory.memballoon.period";
    public static final String VM_MEMORY_MEMBALLOON_MODEL = "vm.memory.memballoon.model";
    public static final String VM_MEMORY_HUGE_PAGES_ENABLE = "vm.memory.huge.pages.enable";
    public static final String VM_MEMORY_HUGE_PAGES_SIZE = "vm.memory.huge.pages.size";
    public static final String VM_CPU_CACHE_ENABLE = "vm.cpu.cache.enable";
    public static final String VM_CLOCK_TYPE = "vm.clock.type";
    public static final String VM_CPU_VIRTUALIZATION_ENABLE = "vm.cpu.virtualization.enable";
    public static final String VM_CPU_VIRTUALIZATION_NAME = "vm.cpu.virtualization.name";


    public static final String VM_CD_BUS = "vm.cd.bus";

    public static final String VM_DEFAULT_UEFI_LOADER_TYPE = "vm.uefi.loader.type";
    public static final String VM_DEFAULT_UEFI_LOADER_PATH = "vm.uefi.loader.path";

    public static final String VM_MACHINE_ARCH = "vm.machine.arch";
    public static final String VM_MACHINE_NAME = "vm.machine.name";


    public static final String STORAGE_LOCAL_ENABLE = "storage.local.enable";
    public static final String STORAGE_LOCAL_PATH = "storage.local.path";
    public static final String STORAGE_NFS_TPL = "storage.nfs.tpl";
    public static final String STORAGE_LOCAL_TPL = "storage.local.tpl";
    public static final String STORAGE_GLUSTERFS_TPL = "storage.glusterfs.tpl";
    public static final String STORAGE_CEPH_RBD_SECRET_TPL = "storage.ceph.rbd.secret.tpl";
    public static final String STORAGE_CEPH_RBD_TPL = "storage.ceph.rbd.tpl";


    public static final String NETWORK_DEFAULT_BRIDGE_TPL = "network.default.bridge.tpl";
    public static final String NETWORK_OVS_BRIDGE_TPL = "network.ovs.bridge.tpl";


    public static final String VM_DOMAIN_TPL = "vm.domain.tpl";

    public static final String VM_PCI_DISK_BUS = "vm.disk.pci.bus";
    public static final String VM_PCI_DISK_SLOT = "vm.disk.pci.slot";
    public static final String VM_PCI_DISK_FUNCTION = "vm.disk.pci.function";

    public static final String VM_PCI_NETWORK_BUS = "vm.network.pci.bus";
    public static final String VM_PCI_NETWORK_SLOT = "vm.network.pci.slot";
    public static final String VM_PCI_NETWORK_FUNCTION = "vm.network.pci.function";

    public static final String VM_DEFAULT_DEVICE_TPL = "vm.device.default.tpl";
    public static final String VM_DISK_NFS_TPL = "vm.disk.nfs.tpl";
    public static final String VM_DISK_GLUSTERFS_TPL = "vm.disk.glusterfs.tpl";
    public static final String VM_DISK_CEPH_RBD_TPL = "vm.disk.ceph.rbd.tpl";
    public static final String VM_DISK_LOCAL_TPL = "vm.disk.ceph.local.tpl";

    public static final String VM_CD_NFS_TPL = "vm.cd.nfs.tpl";
    public static final String VM_CD_GLUSTERFS_TPL = "vm.cd.glusterfs.tpl";
    public static final String VM_CD_CEPH_RBD_TPL = "vm.cd.ceph.rbd.secret.tpl";
    public static final String VM_CD_LOCAL_TPL = "vm.cd.local.tpl";

    public static final String VM_INTERFACE_TPL = "vm.interface.tpl";


    public static final String OAUTH2_ENABLE = "oauth2.enable";
    public static final String OAUTH2_TITLE = "oauth2.title";
    public static final String OAUTH2_CLIENT_ID = "oauth2.client.id";
    public static final String OAUTH2_CLIENT_SECRET = "oauth2.client.secret";
    public static final String OAUTH2_REQUEST_AUTH_URI = "oauth2.request.auth.uri";
    public static final String OAUTH2_REQUEST_USER_URI = "oauth2.request.user.uri";
    public static final String OAUTH2_REQUEST_TOKEN_URI = "oauth2.request.token.uri";
    public static final String OAUTH2_REDIRECT_URI = "oauth2.redirect.uri";
    public static final String OAUTH2_USER_ID_PATH = "oauth2.user.response.id.path";
    public static final String OAUTH2_USER_AUTHORITIES_PATH = "oauth2.user.response.authorities.path";
    public static final String LOGIN_JWD_PASSWORD = "login.jwt.password";
    public static final String LOGIN_JWD_ISSUER = "login.jwt.issuer";
    public static final String LOGIN_JWT_EXPIRE_MINUTES = "login.jwt.expire.minutes";
}
