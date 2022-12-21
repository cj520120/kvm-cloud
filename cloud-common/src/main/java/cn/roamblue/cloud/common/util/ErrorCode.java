package cn.roamblue.cloud.common.util;

/**
 * @author chenjun
 */
public final class ErrorCode {
    public static final int SUCCESS = 0;
    public static final int PARAM_ERROR = 300;
    public static final int SERVER_ERROR = 500;
    public static final int LOCK_TIMEOUT = 504;
    public static final int NO_LOGIN_ERROR = 401;
    public static final int NOT_SUPPORTED = 404;
    public static final int BASE_ERROR = 1000000;
    public static final int BASE_NETWORK_ERROR = BASE_ERROR;
    public static final int BASE_GUEST_ERROR = BASE_ERROR * 2;
    public static final int BASE_STORAGE_ERROR = BASE_ERROR * 3;
    public static final int BASE_VOLUME_ERROR = BASE_ERROR * 4;
    public static final int BASE_TEMPLATE_ERROR = BASE_ERROR * 5;
    public static final int BASE_SNAPSHOT_ERROR = BASE_ERROR * 6;
    public static final int BASE_HOST_ERROR = BASE_ERROR * 7;
    public static final int BASE_SCHEME_ERROR = BASE_ERROR * 8;

    /**
     * GUEST
     */
    public static final int GUEST_NOT_FOUND = BASE_GUEST_ERROR + 1;
    public static final int GUEST_VOLUME_ATTACH_ERROR = BASE_GUEST_ERROR + 2;
    public static final int GUEST_NOT_STOP = BASE_GUEST_ERROR + 3;
    /**
     * Agent
     */
    public static final int VM_COMMAND_ERROR = BASE_GUEST_ERROR + 3;
    /**
     * Volume
     */
    public static final int VOLUME_NOT_FOUND = BASE_VOLUME_ERROR + 1;
    public static final int VOLUME_NOT_READY = BASE_VOLUME_ERROR + 2;
    /**
     * Storage
     */
    public static final int STORAGE_NOT_FOUND = BASE_STORAGE_ERROR + 1;
    public static final int STORAGE_NOT_SPACE = BASE_STORAGE_ERROR + 2;
    public static final int STORAGE_BUSY = BASE_STORAGE_ERROR + 3;

    public static final int STORAGE_NOT_READY = BASE_STORAGE_ERROR + 4;
    /**
     * Template
     */
    public static final int TEMPLATE_NOT_READY = BASE_TEMPLATE_ERROR + 1;
    /**
     * HOst
     */
    public static final int HOST_NOT_FOUND = BASE_HOST_ERROR + 1;
    public static final int HOST_NOT_SPACE = BASE_HOST_ERROR + 2;
    /**
     * Snapshot
     */
    public static final int SNAPSHOT_NOT_FOUND = BASE_SNAPSHOT_ERROR + 1;
    /**
     * Network
     */
    public static final int NETWORK_NOT_FOUND = BASE_NETWORK_ERROR + 1;
    public static final int NETWORK_NOT_SPACE = BASE_NETWORK_ERROR + 2;
    /**
     * Scheme
     */
    public static final int SCHEME_NOT_FOUND = BASE_SCHEME_ERROR + 1;

    private ErrorCode() {

    }
}
