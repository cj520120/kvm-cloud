package cn.roamblue.cloud.management.v2.util;

public class Constant {
    public static class HostStatus{
        public static final int CREATING=0;
        public static final int ONLINE=1;
        public static final int OFFLINE=2;
        public static final int MAINTENANCE=3;
        public static final int ERROR=4;
    }
    public static class StorageStatus{
        public static final int INIT=0;
        public static final int READY=1;
        public static final int DISABLE=2;
        public static final int DESTROY=3;
        public static final int ERROR=3;
    }
    public static class VolumeStatus{
        public static final int CREATING=0;
        public static final int READY=1;
        public static final int ATTACH_DISK=2;
        public static final int DETACH_DISK=3;
        public static final int DESTROY=4;
        public static final int CLONE=5;
        public static final int DOWNLOADING=6;
        public static final int CREATE_TEMPLATE=7;
        public static final int SNAPSHOT=8;
        public static final int MIGRATE=9;
        public static final int RESIZE=10;
        public static final int ERROR=11;
    }
    public static class NetworkStatus{
        public static final int CREATING=1;
        public static final int READY=2;
        public static final int STOPPING=3;
        public static final int STOP=4;
        public static final int DESTROY=5;
        public static final int ERROR=6;
    }
    public static class GuestStatus{

        public static final int CREATING=0;
        public static final int STARTING=1;
        public static final int RUNNING = 2;
        public static final int STOPPING=3;
        public static final int STOP=4;
        public static final int ATTACH_CD_ROOM=5;
        public static final int DETACH_CD_ROOM=6;
        public static final int ATTACH_DISK=7;
        public static final int DETACH_DISK=8;
        public static final int ATTACH_NIC=9;
        public static final int DETACH_NIC=10;
        public static final int DESTROY=11;
        public static final int REBOOT = 12;
        public static final int ERROR=13;
    }

    public static class NetworkType{
        public static final int BASIC = 0;
        public static final int VLAN=1;

    }
}
