package cn.roamblue.cloud.common.util;

/**
 * @author chenjun
 */
public final class ErrorCode {
    public static final int BASE_ERROR = 1000000;
    public static final int SUCCESS = 0;
    public static final int PARAM_ERROR = 300;
    public static final int SERVER_ERROR = 500;
    public static final int LOCK_TIMEOUT = 504;
    public static final int NO_LOGIN_ERROR = 401;
    public static final int NOT_SUPPORTED = 404;
    public static final int VM_NOT_FOUND = BASE_ERROR + 1;
    public static final int STORAGE_NOT_FOUND = BASE_ERROR + 2;
    public static final int TEMPLATE_NOT_READY = BASE_ERROR + 4;
    public static final int VM_COMMAND_ERROR = BASE_ERROR + 5;
    public static final int HOST_NOT_FOUND = BASE_ERROR + 12;
    public static final int VOLUME_NOT_FOUND = BASE_ERROR + 14;
    public static final int STORAGE_NOT_SPACE = BASE_ERROR + 15;
    public static final int SNAPSHOT_NOT_FOUND = BASE_ERROR + 15;
    public static final int NETWORK_NOT_FOUND = BASE_ERROR + 16;
    public static final int HOST_NOT_SPACE = BASE_ERROR + 18;
    public static final int NETWORK_NOT_SPACE = BASE_ERROR + 19;
    public static final int VOLUME_ATTACH_ERROR = BASE_ERROR + 20;
    public static final int VOLUME_NOT_READY = BASE_ERROR + 23;
    public static final int STORAGE_NOT_READY = BASE_ERROR + 25;
    public static final int VM_NOT_STOP = BASE_ERROR + 27;
    public static final int STORAGE_BUSY = BASE_ERROR + 42;
    public static final int SCHEME_NOT_FOUND = BASE_ERROR + 42;

    private ErrorCode() {

    }
}
