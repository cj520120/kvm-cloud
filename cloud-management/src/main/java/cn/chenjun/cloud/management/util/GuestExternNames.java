package cn.chenjun.cloud.management.util;

public class GuestExternNames {
    public static final String VNC = "vnc";
    public static final String META_DATA = "meta-data";
    public static final String USER_DATA = "user-data";

    public static final class VncNames {
        public static final String PASSWORD = "password";
        public static final String PORT = "port";
        public static final String HOST = "host";
    }

    public static final class MetaDataNames {
        public static final String HOSTNAME = "hostname";
        public static final String LOCAL_HOSTNAME = "local-hostname";
        public static final String INSTANCE_ID = "instance-id";
    }

    public static final class UserDataNames {
        public static final String PASSWORD_IV_KEY = "password-iv-key";
        public static final String PASSWORD_ENCODE_KEY = "password-encode-key";
        public static final String PASSWORD = "password";
        public static final String SSH_PUBLIC_KEY = "ssh-public-key";
    }
}
