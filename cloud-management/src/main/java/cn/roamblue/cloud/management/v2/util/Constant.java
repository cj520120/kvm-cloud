package cn.roamblue.cloud.management.v2.util;

public class Constant {
    public static class HostStatus{
        public static final int INIT=0;
        public static final int READY=1;
        public static final int DISABLE=2;
        public static final int DESTROY=3;
        public static final int ERROR=3;
    }
    public static class StorageStatus{
        public static final int INIT=0;
        public static final int READY=1;
        public static final int DISABLE=2;
        public static final int DESTROY=3;
        public static final int ERROR=3;
    }
    public static class VolumeStatus{
        public static final int INIT=0;
        public static final int READY=1;
        public static final int ATTACH=1;
        public static final int DETACH=2;
        public static final int DESTROY=3;
        public static final int ERROR=3;
    }
    public static class NetworkStatus{
        public static final int READY=1;
        public static final int DISABLE=2;
        public static final int DESTROY=3;
        public static final int ERROR=3;
    }
    public static class GuestStatus{

        public static final int INIT=0;
        public static final int START=1;
        public static final int STOP=2;
        public static final int DESTROY=3;
        public static final int ERROR=3;
        public static final int RUNNING = 1;
    }

    public static class NetworkType{
        public static final int BASIC = 0;
        public static final int VLAN=1;

    }
}
