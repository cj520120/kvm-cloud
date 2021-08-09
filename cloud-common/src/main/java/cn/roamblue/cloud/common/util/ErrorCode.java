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
    public static final int TEMPLATE_NOT_FOUND = BASE_ERROR + 3;
    public static final int TEMPLATE_NOT_READY = BASE_ERROR + 4;
    public static final int VM_COMMAND_ERROR = BASE_ERROR + 5;
    public static final int CLUSTER_NOT_FOUND = BASE_ERROR + 6;
    public static final int HAS_NETWORK_ERROR = BASE_ERROR + 7;
    public static final int HAS_STORAGE_ERROR = BASE_ERROR + 8;
    public static final int HAS_HOST_ERROR = BASE_ERROR + 9;
    public static final int HAS_VOLUME_ERROR = BASE_ERROR + 10;
    public static final int HAS_VM_ERROR = BASE_ERROR + 11;
    public static final int HOST_NOT_FOUND = BASE_ERROR + 12;
    public static final int HOST_EXISTS = BASE_ERROR + 13;
    public static final int VOLUME_NOT_FOUND = BASE_ERROR + 14;
    public static final int STORAGE_NOT_SPACE = BASE_ERROR + 15;
    public static final int NETWORK_NOT_FOUND = BASE_ERROR + 16;
    public static final int TEMPLATE_STORAGE_NOT_READY = BASE_ERROR + 17;
    public static final int HOST_NOT_SPACE = BASE_ERROR + 18;
    public static final int NETWORK_NOT_SPACE = BASE_ERROR + 19;
    public static final int VOLUME_ATTACH_ERROR = BASE_ERROR + 20;
    public static final int AGENT_VM_NOT_FOUND = BASE_ERROR + 21;
    public static final int AGENT_STORAGE_NOT_FOUND = BASE_ERROR + 22;
    public static final int VOLUME_NOT_READY = BASE_ERROR + 23;
    public static final int NETWORK_NOT_READY = BASE_ERROR + 24;
    public static final int STORAGE_NOT_READY = BASE_ERROR + 25;
    public static final int VM_NOT_START = BASE_ERROR + 26;
    public static final int VM_NOT_STOP = BASE_ERROR + 27;
    public static final int PERMISSION_ERROR = BASE_ERROR + 28;
    public static final int CALCULATION_SCHEME_NOT_FOUND = BASE_ERROR + 29;
    public static final int USER_FORBID_ERROR = BASE_ERROR + 30;
    public static final int OLD_PASSWORD_ERROR = BASE_ERROR + 31;
    public static final int PASSWORD_EMPTY_ERROR = BASE_ERROR + 32;
    public static final int OS_CATEGORY_NOT_FOUND = BASE_ERROR + 33;
    public static final int GROUP_NOT_FOUND = BASE_ERROR + 34;
    public static final int LOGIN_USER_EXISTS = BASE_ERROR + 35;
    public static final int QEMU_NOT_CONNECT = BASE_ERROR + 36;
    public static final int DETACH_NETWORK_ERROR = BASE_ERROR + 38;
    public static final int USER_LOGIN_NAME_OR_PASSWORD_ERROR = BASE_ERROR + 39;
    public static final int RULE_PERMISSION_NOT_FOUND = BASE_ERROR + 40;

    private ErrorCode() {

    }
}
