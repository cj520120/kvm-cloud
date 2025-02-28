package cn.chenjun.cloud.management.util;

/**
 * @author chenjun
 */
public class Constant {
    public static int MAX_DEVICE_ID = 5;

    public static class NetworkAllocateType {
        public static final int GUEST = 0;
        public static final int COMPONENT_VIP = 1;
    }

    public static class HostStatus {
        public static final int REGISTER = 0;
        public static final int ONLINE = 1;
        public static final int OFFLINE = 2;
        public static final int MAINTENANCE = 3;
        public static final int ERROR = 4;
    }

    public static class StorageStatus {
        public static final int INIT = 0;
        public static final int READY = 1;
        public static final int MAINTENANCE = 2;
        public static final int DESTROY = 3;
        public static final int ERROR = 4;
    }

    public static class StorageSupportCategory {
        public static final int TEMPLATE = 1 << 0;
        public static final int VOLUME = 1 << 1;
    }

    public static class SnapshotStatus {
        public static final int CREATING = 0;
        public static final int READY = 1;
        public static final int ERROR = 2;
        public static final int DESTROY = 3;
    }

    public static class VolumeStatus {
        public static final int CREATING = 0;
        public static final int READY = 1;
        public static final int ATTACH_DISK = 2;
        public static final int DETACH_DISK = 3;
        public static final int CLONE = 4;
        public static final int CREATE_TEMPLATE = 5;
        public static final int CREATE_SNAPSHOT = 6;
        public static final int MIGRATE = 7;
        public static final int RESIZE = 8;
        public static final int DESTROY = 9;
        public static final int ERROR = 10;
    }

    public static class NetworkStatus {
        public static final int CREATING = 1;
        public static final int READY = 2;
        public static final int MAINTENANCE = 3;
        public static final int DESTROY = 4;
        public static final int INSTALL = 5;
        public static final int ERROR = 6;
    }

    public static class GuestStatus {

        public static final int CREATING = 0;
        public static final int STARTING = 1;
        public static final int RUNNING = 2;
        public static final int STOPPING = 3;
        public static final int STOP = 4;
        public static final int REBOOT = 5;

        public static final int ERROR = 6;
        public static final int MIGRATE = 7;

        public static final int DESTROY = 8;
    }

    public static class TemplateStatus {
        public static final int CREATING = 0;
        public static final int DOWNLOAD = 1;
        public static final int READY = 2;
        public static final int ERROR = 3;
        public static final int DESTROY = 4;
    }

    public static class NetworkType {
        public static final int BASIC = 0;
        public static final int VLAN = 1;

    }

    public static class TemplateType {
        public static final int ISO = 0;
        public static final int SYSTEM = 1;
        public static final int VOLUME = 2;
    }

    public static class GuestType {
        public static final int COMPONENT = 0;
        public static final int USER = 1;
    }

    public static class ComponentType {
        public static final int ROUTE = 1;
        public static final int NAT = 2;

    }

    public static class OperateType {
        public static int DESTROY_HOST_STORAGE = 1;
        public static int SYNC_HOST_GUEST = 2;
        public static int DESTROY_GUEST = 3;
        public static int START_COMPONENT_GUEST = 4;
        public static int CREATE_STORAGE = 5;
        public static int DESTROY_STORAGE = 6;
        public static int CHANGE_GUEST_NETWORK_INTERFACE = 7;
        public static int INIT_HOST_STORAGE = 8;
        public static int DESTROY_HOST_NETWORK = 9;
        public static int DESTROY_SNAPSHOT_VOLUME = 10;
        public static int RESIZE_VOLUME = 11;
        public static int CREATE_GUEST = 12;
        public static int INIT_HOST_NETWORK = 13;
        public static int VOLUME_CHECK = 14;
        public static int SYNC_HOST_TASK_ID = 15;
        public static int DESTROY_TEMPLATE = 16;
        public static int CHANGE_GUEST_CD_ROOM = 17;
        public static int REBOOT_GUEST = 18;
        public static int CREATE_VOLUME_SNAPSHOT = 19;
        public static int STOP_GUEST = 20;
        public static int CREATE_VOLUME_TEMPLATE = 21;
        public static int MIGRATE_VOLUME = 22;
        public static int GUEST_INFO = 23;
        public static int CLONE_VOLUME = 24;
        public static int CREATE_HOST = 25;
        public static int DESTROY_HOST_GUEST = 26;
        public static int DOWNLOAD_TEMPLATE = 27;
        public static int CREATE_NETWORK = 28;
        public static int START_GUEST = 29;
        public static int CREATE_VOLUME = 30;
        public static int DESTROY_VOLUME = 31;
        public static int CHANGE_GUEST_DISK = 32;
        public static int DESTROY_NETWORK = 33;
        public static int MIGRATE_GUEST = 34;
        public static int HOST_CHECK = 35;
        public static int STORAGE_CHECK = 36;
    }

    public class UserState {
        /**
         * 启用
         */
        public static final short ABLE = 0;
        /**
         * 禁用
         */
        public static final short DISABLE = 1;
    }

    public static class UserType {
        public static final String LOCAL = "Local";
        public static final String OAUTH2 = "Oauth2";
    }

    public static class HttpHeaderNames {
        /**
         * 登陆HTTP TOKEN 头
         */
        public static final String TOKEN_HEADER = "X-TOKEN";
        /**
         * 登陆用户HTTP 上下文
         */
        public static final String LOGIN_USER_INFO_ATTRIBUTE = "X-USER-INFO";

    }

    public static class WsClientType {
        public static final short WEB = 0;
        public static final short COMPONENT = 1;

    }

    public static class ConfigKey {
        public static final String SYSTEM_COMPONENT_NETWORK_DRIVER = "system.component.network.driver";
        public static final String SYSTEM_COMPONENT_NETWORK_CHECK_ADDRESS = "system.component.network.check.address";
        public static final String SYSTEM_COMPONENT_CPU = "system.component.cpu.number";
        public static final String SYSTEM_COMPONENT_MEMORY = "system.component.memory";
        public static final String SYSTEM_COMPONENT_CPU_SHARE = "system.component.cpu.share";
        public static final String SYSTEM_COMPONENT_QMA_EXECUTE_TIMEOUT_MINUTES = "system.component.qma.execute.timeout.minutes";
        public static final String SYSTEM_COMPONENT_QMA_CHECK_TIMEOUT_MINUTES = "system.component.qma.check.timeout.minutes";
        public static final String SYSTEM_COMPONENT_PIP_INSTALL_SOURCE = "system.component.pip.source";
        public static final String SYSTEM_COMPONENT_YUM_INSTALL_SOURCE = "system.component.yum.repo";
        public static final String DEFAULT_CLUSTER_OVER_CPU = "default.cluster.over.cpu";
        public static final String DEFAULT_CLUSTER_DISK_TYPE = "default.cluster.disk.type";
        public static final String DEFAULT_CLUSTER_OVER_MEMORY = "default.cluster.over.memory";
        public static final String DEFAULT_CLUSTER_MANAGER_URI = "default.cluster.manager.uri";
        public static final String DEFAULT_CLUSTER_DESTROY_DELAY_MINUTE = "default.cluster.destroy.delay.timeout.minutes";

        public static final String DEFAULT_CLUSTER_TASK_CLEAR_COMPONENT_TIMEOUT_SECOND = "default.cluster.component.clear.timeout.second";
        public static final String DEFAULT_CLUSTER_TASK_COMPONENT_CHECK_TIMEOUT_SECOND = "default_cluster_component.check.timeout.second";
        public static final String DEFAULT_CLUSTER_TASK_HOST_GUEST_SYNC_CHECK_TIMEOUT_SECOND = "default.cluster.host.guest.sync.check.timeout.second";
        public static final String DEFAULT_CLUSTER_TASK_HOST_TASK_SYNC_CHECK_TIMEOUT_SECOND = "default.cluster.host.task.sync.check.timeout.second";
        public static final String DEFAULT_CLUSTER_TASK_HOST_CHECK_TIMEOUT_SECOND = "default.cluster.host.check.timeout.second";
        public static final String DEFAULT_CLUSTER_TASK_STORAGE_CHECK_TIMEOUT_SECOND = "default.cluster.storage.check.timeout.second";
        public static final String DEFAULT_CLUSTER_TASK_STORAGE_VOLUME_SYNC_TIMEOUT_SECOND = "default.cluster.storage.volume.sync.timeout.second";
        public static final String DEFAULT_CLUSTER_TASK_EXPIRE_TIMEOUT_SECOND = "default.cluster.task.expire.timeout.second";

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


        public static final String STORAGE_NFS_TPL = "storage.nfs.tpl";
        public static final String STORAGE_GLUSTERFS_TPL = "storage.glusterfs.tpl";
        public static final String STORAGE_CEPH_RBD_SECRET_TPL = "storage.ceph.rbd.secret.tpl";
        public static final String STORAGE_CEPH_RBD_TPL = "storage.ceph.rbd.tpl";


        public static final String NETWORK_DEFAULT_BRIDGE_TPL = "network.default.bridge.tpl";
        public static final String NETWORK_OVS_BRIDGE_TPL = "network.ovs.bridge.tpl";


        public static final String VM_DOMAIN_TPL = "vm.domain.tpl";


        public static final String VM_DISK_NFS_TPL = "vm.disk.nfs.tpl";
        public static final String VM_DISK_GLUSTERFS_TPL = "vm.disk.glusterfs.tpl";
        public static final String VM_DISK_CEPH_RBD_TPL = "vm.disk.ceph.rbd.tpl";

        public static final String VM_CD_NFS_TPL = "vm.cd.nfs.tpl";
        public static final String VM_CD_GLUSTERFS_TPL = "vm.cd.glusterfs.tpl";
        public static final String VM_CD_CEPH_RBD_TPL = "vm.cd.ceph.rbd.secret.tpl";

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

    public static class ConfigValueType {
        public static final int STRING = 0;
        public static final int MULTI_STRING = 1;
        public static final int INT = 2;
        public static final int FLOAT = 3;
        public static final int SELECT = 4;
    }

    public static class ConfigAllocateType {
        public static final int DEFAULT = 0;
        public static final int HOST = 1;
        public static final int GUEST = 2;
    }

    public static class Enable {
        public static final String YES = "yes";
        public static final String NO = "no";
    }
}
