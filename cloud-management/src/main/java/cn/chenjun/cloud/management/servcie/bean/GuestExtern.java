package cn.chenjun.cloud.management.servcie.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class GuestExtern {
    @SerializedName(GuestExternNames.META_DATA)
    private MetaData metaData;
    @SerializedName(GuestExternNames.USER_DATA)
    private UserData userData;
    @SerializedName(GuestExternNames.VNC)
    private Vnc vnc;

    @Data
    public static class MetaData {
        @SerializedName(GuestExternNames.MetaDataNames.HOSTNAME)
        private String hostname;
        @SerializedName(GuestExternNames.MetaDataNames.LOCAL_HOSTNAME)
        private String localHostname;
        @SerializedName(GuestExternNames.MetaDataNames.INSTANCE_ID)
        private String instanceId;
    }

    @Data
    public static class UserData {
        @SerializedName(GuestExternNames.UserDataNames.PASSWORD_IV_KEY)
        private String passwordIvKey;
        @SerializedName(GuestExternNames.UserDataNames.PASSWORD_ENCODE_KEY)
        private String passwordEncodeKey;
        @SerializedName(GuestExternNames.UserDataNames.PASSWORD)
        private String password;
        @SerializedName(GuestExternNames.UserDataNames.SSH_PUBLIC_KEY)
        private String sshPublicKey;

    }

    @Data
    public static class Vnc {
        @SerializedName(GuestExternNames.VncNames.PASSWORD)
        private String password;
        @SerializedName(GuestExternNames.VncNames.PORT)
        private String port;
        @SerializedName(GuestExternNames.VncNames.HOST)
        private String host;

    }

    public static class GuestExternNames {
        public static final String META_DATA = "meta-data";
        public static final String USER_DATA = "user-data";
        public static final String VNC = "vnc";

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
}
