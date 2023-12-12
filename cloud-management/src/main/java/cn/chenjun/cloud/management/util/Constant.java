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
        public static final int ERROR = 5;
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

    public class UserType {
        public static final String LOCAL = "Local";
        public static final String OAUTH2 = "Oauth2";
    }

    public class HttpHeaderNames {
        /**
         * 登陆HTTP TOKEN 头
         */
        public static final String TOKEN_HEADER = "X-TOKEN";
        /**
         * 登陆用户HTTP 上下文
         */
        public static final String LOGIN_USER_INFO_ATTRIBUTE = "X-USER-INFO";

    }

    public class WsClientType{
        public static final short WEB=0;
        public static final short COMPONENT=1;

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
}
