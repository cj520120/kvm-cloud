package cn.roamblue.cloud.common.util;

/**
 * @author chenjun
 */
public class Command {
    public static final class Storage{
        public static final String CREATE="create";
        public static final String DESTROY = "destroy";
    }
    public static final class Network{
        public static final String CREATE="create";
        public static final String DESTROY = "destroy";
    }
    public static final class Volume{
        public static final String CREATE="create";
        public static final String DESTROY = "destroy";
        public static final String CLONE="clone";
        public static final String RESIZE = "resize";

        public static final String MIGRATE = "migrate";
        public static final String SNAPSHOT = "snapshot";

        public static final String TEMPLATE = "template";
        public static final String DOWNLOAD = "download";
    }

    public static final class  Os{
        public static final String DESTROY = "delete";
        public static final String START = "start";
        public static final String STOP = "stop";
        public static final String REBOOT = "reboot";
        public static final String SHUTDOWN = "shutdown";
        public static final String QMA = "qma";
    }
}
