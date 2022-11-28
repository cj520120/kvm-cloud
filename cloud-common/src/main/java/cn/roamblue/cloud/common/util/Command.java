package cn.roamblue.cloud.common.util;

/**
 * @author chenjun
 */
public class Command {
    public static final class Storage{
        public static final String CREATE="CreateStorage";
        public static final String DESTROY = "DestroyStorage";
    }
    public static final class Network{
        public static final String CREATE_BASIC ="CreateBasicNetwork";
        public static final String DESTROY_BASIC = "DestroyBasicNetwork";
        public static final String CREATE_VLAN ="CreateVlanNetwork";
        public static final String DESTROY_VLAN = "DestroyVlanNetwork";
    }
    public static final class Volume{
        public static final String CREATE="CreateVolume";
        public static final String DESTROY = "DestroyVolume";
        public static final String CLONE="CloneVolume";
        public static final String RESIZE = "ResizeVolume";

        public static final String MIGRATE = "MigrateVolume";
        public static final String SNAPSHOT = "SnapshotVolume";

        public static final String TEMPLATE = "TemplateVolume";
        public static final String DOWNLOAD = "DownloadVolume";
    }

    public static final class  Os{
        public static final String DESTROY = "DeleteOs";
        public static final String START = "StartOs";
        public static final String REBOOT = "RebootOs";
        public static final String SHUTDOWN = "ShutdownOs";
        public static final String QMA = "QMAOs";
    }
}
