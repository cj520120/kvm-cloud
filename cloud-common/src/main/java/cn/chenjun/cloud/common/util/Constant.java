package cn.chenjun.cloud.common.util;

import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * @author chenjun
 */
public class Constant {
    public static int MAX_DEVICE_COUNT = 200;

    public enum NetworkBridgeType {

        /**
         * 基础网络
         */
        BASIC(0, "bridge"),
        OPEN_SWITCH(1, "OpenSwitch");
        private final String bridgeName;
        private final int bridgeType;

        NetworkBridgeType(int bridgeType, String bridgeName) {
            this.bridgeType = bridgeType;
            this.bridgeName = bridgeName;
        }

        public static NetworkBridgeType fromBridgeValue(String value) {
            return Arrays.stream(NetworkBridgeType.values()).filter(br -> StringUtils.endsWithIgnoreCase(br.bridgeName, value)).findFirst().orElse(null);
        }

        public static NetworkBridgeType fromBridgeType(int type) {
            return Arrays.stream(NetworkBridgeType.values()).filter(br -> br.bridgeType == type).findFirst().orElse(null);
        }

        public int bridgeType() {
            return this.bridgeType;
        }

        public String bridgeName() {
            return this.bridgeName;
        }
    }

    public static class VolumeType {
        public static final String RAW = "raw";
        public static final String QCOW = "qcow";
        public static final String QCOW2 = "qcow2";
        public static final String VDI = "vdi";
        public static final String VMDK = "vmdk";
        public static final String VPC = "vpc";

    }
    public static class  DeviceType {
        public static final String DISK = "disk";
        public static final String BLOCK = "block";
        public static final String FILE = "file";
    }

    public static class Command {
        public static final String CHECK_TASK = "CheckTask";
        public static final String HOST_INFO = "HostInfo";
        public static final String HOST_INIT = "HostInit";
        public static final String STORAGE_INFO = "StorageInfo";
        public static final String BATCH_STORAGE_INFO = "BatchStorageInfo";
        public static final String STORAGE_CREATE = "CreateStorage";
        public static final String STORAGE_DESTROY = "DestroyStorage";

        public static final String NETWORK_CREATE_BASIC = "CreateBasicNetwork";
        public static final String NETWORK_DESTROY_BASIC = "DestroyBasicNetwork";
        public static final String NETWORK_CREATE_VLAN = "CreateVlanNetwork";
        public static final String NETWORK_DESTROY_VLAN = "DestroyVlanNetwork";
        public static final String VOLUME_INFO = "VolumeInfo";
        public static final String BATCH_VOLUME_INFO = "BatchVolumeInfo";
        public static final String VOLUME_CREATE = "CreateVolume";
        public static final String VOLUME_DESTROY = "DestroyVolume";
        public static final String DESTROY_UNLINK_VOLUME = "DestroyUnLinkVolume";
        public static final String VOLUME_CLONE = "CloneVolume";
        public static final String VOLUME_RESIZE = "ResizeVolume";
        public static final String LIST_STORAGE_VOLUME = "ListStorageVolume";

        public static final String VOLUME_MIGRATE = "MigrateVolume";
        public static final String VOLUME_DOWNLOAD = "DownloadVolume";

        public static final String GUEST_DESTROY = "DeleteGuest";
        public static final String GUEST_START = "StartGuest";
        public static final String GUEST_INFO = "GuestInfo";
        public static final String BATCH_GUEST_INFO = "BatchGuestInfo";
        public static final String ALL_GUEST_INFO = "AllGuestInfo";
        public static final String GUEST_REBOOT = "RebootGuest";
        public static final String GUEST_SHUTDOWN = "ShutdownGuest";
        public static final String GUEST_MIGRATE = "MigrateGuest";

        public static final String GUEST_ATTACH_CD_ROOM = "AttachCdRoom";
        public static final String GUEST_DETACH_CD_ROOM = "DetachCdRoom";
        public static final String GUEST_ATTACH_DISK = "AttachDisk";
        public static final String GUEST_DETACH_DISK = "DetachDisk";
        public static final String GUEST_ATTACH_NIC = "AttachNic";
        public static final String GUEST_DETACH_NIC = "DetachNic";
        public static final String GUEST_QMA = "QMAGuest";

    }


    public static class StorageType {
        public static final String NFS = "nfs";
        public static final String GLUSTERFS = "glusterfs";
        public static final String CEPH_RBD = "ceph-rbd";
        public static final String LOCAL = "local";
    }

    public static class DiskDriveType {
        public static final String VIRTIO = "virtio";
        public static final String IDE = "ide";
        public static final String SCSI = "scsi";
        public static final String SATA = "sata";

    }

    public static class NetworkDriver {
        public static final String VIRTIO = "virtio";
        public static final String RTL8139 = "rtl8139";
        public static final String E1000 = "e1000";

    }

    public static class SocketCommand {


        public static final int WEB_LOGIN = 100;
        public static final int WEB_LOGIN_SUCCESS = 101;
        public static final int WEB_LOGIN_TOKEN_ERROR = 102;
        public static final int WEB_NOTIFY = 103;


        public static final int COMPONENT_CONNECT = 200;
        public static final int COMPONENT_CONNECT_SUCCESS = 201;
        public static final int COMPONENT_CONNECT_FAIL = 202;
        public static final int COMPONENT_NOTIFY = 203;
        public static final int COMPONENT_DNS_REQUEST = 206;
        public static final int COMPONENT_NAT_REQUEST = 207;


    }

    public static class NotifyType {

        public static final int UPDATE_GUEST = 1;

        public static final int UPDATE_VOLUME = 2;
        public static final int UPDATE_NETWORK = 3;
        public static final int UPDATE_HOST = 4;
        public static final int UPDATE_TEMPLATE = 5;
        public static final int UPDATE_STORAGE = 7;
        public static final int UPDATE_SCHEME = 8;
        public static final int UPDATE_GROUP = 9;
        public static final int UPDATE_DNS = 10;
        public static final int UPDATE_COMPONENT = 11;
        public static final int UPDATE_COMPONENT_NAT = 12;
        public static final int UPDATE_SSH = 13;

        public static final int UPDATE_USER = 14;

        public static final int COMPONENT_UPDATE_DNS = 101;
        public static final int COMPONENT_UPDATE_NAT = 102;



        public static final int GUEST_START_CALLBACK_NOTIFY = 301;
        public static final int GUEST_STOP_CALLBACK_NOTIFY = 302;
        public static final int GUEST_RESTART_CALLBACK_NOTIFY = 303;
    }

    /**
     * @author chenjun
     */
    public static class BootstrapType {
        public static final int BIOS = 0;
        public static final int UEFI = 1;
        public static final String UEFI_STR = "UEFI";
        public static final String BIOS_STR = "BIOS";
    }

    public static class Enable {
        public static final String YES = "yes";
        public static final String NO = "no";
    }

    public static class ComponentType {
        public static final int ROUTE = 1;
        public static final int NAT = 2;

    }

    public static class ConfigType {
        public static final int DEFAULT = 0;
        public static final int HOST = 1;
        public static final int GUEST = 2;
        public static final int NETWORK = 3;
        public static final int STORAGE = 4;

        public static final int VOLUME = 5;
    }

    public static class ConfigValueType {
        public static final int STRING = 0;
        public static final int MULTI_STRING = 1;
        public static final int INT = 2;
        public static final int FLOAT = 3;
        public static final int SELECT = 4;
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

    public static class GuestType {
        public static final int COMPONENT = 0;
        public static final int USER = 1;
    }

    public static class HostStatus {
        public static final int REGISTER = 0;
        public static final int ONLINE = 1;
        public static final int OFFLINE = 2;
        public static final int MAINTENANCE = 3;
        public static final int ERROR = 4;
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

    public static class LoginType {
        public static final short LOCAL = 0;
        public static final short OAUTH2 = 1;
    }

    public static class NetworkAllocateType {
        public static final int DEFAULT = 0;
        public static final int GUEST = 1;
        public static final int COMPONENT_VIP = 2;
        public static final int CUSTOM = 3;
    }

    public static class NetworkStatus {
        public static final int CREATING = 1;
        public static final int READY = 2;
        public static final int MAINTENANCE = 3;
        public static final int DESTROY = 4;
        public static final int INSTALL = 5;
        public static final int ERROR = 6;
    }

    public static class NetworkType {
        public static final int BASIC = 0;
        public static final int VLAN = 1;

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
        public static int RESIZE_VOLUME = 11;
        public static int CREATE_GUEST = 12;
        public static int INIT_HOST_NETWORK = 13;
        public static int VOLUME_CHECK = 14;
        public static int SYNC_HOST_TASK_ID = 15;
        public static int DESTROY_TEMPLATE = 16;
        public static int CHANGE_GUEST_CD_ROOM = 17;
        public static int REBOOT_GUEST = 18;
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
        public static int MIGRATE_TEMPLATE_VOLUME = 37;
        public static int DESTROY_TEMPLATE_VOLUME = 38;
        public static int STORAGE_VOLUME_CLEAR = 39;
        public static int CREATE_GUEST_VOLUME = 40;
    }


    public static class StorageStatus {
        public static final int INIT = 0;
        public static final int READY = 1;
        public static final int MAINTENANCE = 2;
        public static final int DESTROY = 3;
        public static final int ERROR = 4;
    }

    public static class StorageCategory {
        public static final int TEMPLATE = 1;
        public static final int VOLUME = 1 << 1;
    }

    public static class TemplateStatus {
        public static final int CREATING = 0;
        public static final int DOWNLOAD = 1;
        public static final int READY = 2;
        public static final int ERROR = 3;
        public static final int DESTROY = 4;
        public static final Integer MIGRATE = 5;
    }

    public static class TemplateType {
        public static final int ISO = 0;
        public static final int SYSTEM = 1;
        public static final int VOLUME = 2;
    }

    public static class UserState {
        /**
         * 启用
         */
        public static final short ABLE = 0;
        /**
         * 禁用
         */
        public static final short DISABLE = 1;
    }

    public static class VolumeStatus {
        public static final int CREATING = 0;
        public static final int READY = 1;
        public static final int CLONE = 4;
        public static final int CREATE_TEMPLATE = 5;
        public static final int MIGRATE = 7;
        public static final int RESIZE = 8;
        public static final int DESTROY = 9;
        public static final int ERROR = 10;
    }

    public static class UserType {
        public static final short SUPPER_ADMIN = 0;
        public static final short ADMIN = 100;
        public static final short USER = 200;
    }

    /**
     * @author chenjun
     */
    public static class SystemCategory {
        public static final int BASE_LINUX = 100;
        public static final int OTHER_UNIX = 200;
        public static final int WINDOWS = 300;
        public static final int ANDROID = 400;
        public static final int CENTOS = BASE_LINUX + 1;
        public static final int UBUNTU = BASE_LINUX + 2;
        public static final int DEEPIN = BASE_LINUX + 3;
        public static final int RED_HAT = BASE_LINUX + 4;
        public static final int DEBIAN = BASE_LINUX + 5;
        public static final int OPEN_EULER = BASE_LINUX + 6;
        public static final int UOS = BASE_LINUX + 7;
        public static final int ORACLE_LINUX = BASE_LINUX + 8;
    }
}
