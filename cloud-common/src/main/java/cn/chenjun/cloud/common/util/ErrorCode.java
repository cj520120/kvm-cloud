package cn.chenjun.cloud.common.util;

/**
 * @author chenjun
 */
public final class ErrorCode {
    public static final int SUCCESS = 0;
    public static final int PARAM_ERROR = 300;
    public static final int SERVER_ERROR = 500;
    public static final int NO_LOGIN_ERROR = 401;
    public static final int BASE_ERROR = 1000000;
    public static final int BASE_NETWORK_ERROR = BASE_ERROR;
    /**
     * Network
     */
    public static final int NETWORK_NOT_FOUND = BASE_NETWORK_ERROR + 1;
    public static final int NETWORK_NOT_SPACE = BASE_NETWORK_ERROR + 2;
    public static final int NETWORK_COMPONENT_NOT_FOUND = BASE_NETWORK_ERROR + 3;
    public static final int NETWORK_COMPONENT_NAT_NOT_FOUND = BASE_NETWORK_ERROR + 4;
    public static final int BASE_GUEST_ERROR = BASE_ERROR * 2;
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
    public static final int BASE_STORAGE_ERROR = BASE_ERROR * 3;
    /**
     * Storage
     */
    public static final int STORAGE_NOT_FOUND = BASE_STORAGE_ERROR + 1;
    public static final int STORAGE_NOT_SPACE = BASE_STORAGE_ERROR + 2;
    public static final int STORAGE_BUSY = BASE_STORAGE_ERROR + 3;
    public static final int STORAGE_NOT_READY = BASE_STORAGE_ERROR + 4;
    public static final int STORAGE_NOT_SUPPORT = BASE_STORAGE_ERROR + 5;
    /**
     * Volume
     */
    public static final int BASE_VOLUME_ERROR = BASE_ERROR * 4;
    public static final int VOLUME_NOT_FOUND = BASE_VOLUME_ERROR + 1;
    public static final int VOLUME_NOT_READY = BASE_VOLUME_ERROR + 2;
    public static final int BASE_TEMPLATE_ERROR = BASE_ERROR * 5;
    /**
     * Template
     */
    public static final int TEMPLATE_NOT_FOUND = BASE_TEMPLATE_ERROR + 1;
    public static final int TEMPLATE_NOT_READY = BASE_TEMPLATE_ERROR + 2;
    public static final int BASE_SNAPSHOT_ERROR = BASE_ERROR * 6;
    /**
     * Snapshot
     */
    public static final int SNAPSHOT_NOT_FOUND = BASE_SNAPSHOT_ERROR + 1;
    public static final int BASE_HOST_ERROR = BASE_ERROR * 7;
    /**
     * Host
     */
    public static final int HOST_NOT_FOUND = BASE_HOST_ERROR + 1;
    public static final int HOST_NOT_SPACE = BASE_HOST_ERROR + 2;
    /**
     * Scheme
     */
    public static final int BASE_SCHEME_ERROR = BASE_ERROR * 8;
    public static final int SCHEME_NOT_FOUND = BASE_SCHEME_ERROR + 1;
    public static final int BASE_USER_ERROR = BASE_ERROR * 9;
    public static final int USER_LOGIN_NAME_OR_PASSWORD_ERROR = BASE_USER_ERROR + 1;
    public static final int USER_FORBID_ERROR = BASE_USER_ERROR + 2;
    public static final int PERMISSION_ERROR = BASE_USER_ERROR + 3;
    /**
     * Group
     */
    public static final int BASE_GROUP_ERROR = BASE_ERROR * 10;
    public static final int GROUP_NOT_FOUND = BASE_GROUP_ERROR + 1;
    /**
     * DNS
     */
    public static final int BASE_DNS_ERROR = BASE_ERROR * 11;
    public static final int DNS_NOT_FOUND = BASE_DNS_ERROR + 1;
    /**
     * SSh
     */
    public static final int BASE_SSH_AUTHORIZED_ERROR = BASE_ERROR * 12;
    public static final int SSH_AUTHORIZED_NOT_FOUND = BASE_SSH_AUTHORIZED_ERROR + 1;
    public static final int SSH_AUTHORIZED_CREATE_ERROR = BASE_SSH_AUTHORIZED_ERROR + 2;

    /**
     * Agent
     */
    public static final int BASE_AGENT_ERROR = BASE_ERROR * 13;
    public static final int AGENT_TASK_ASYNC_WAIT = BASE_AGENT_ERROR + 1;
    public static final int BASE_CONFIG_ERROR = BASE_ERROR * 14;
    /**
     * Config
     */
    public static final int CONFIG_EXISTS_ERROR = BASE_CONFIG_ERROR + 1;
    public static final int CONFIG_NOT_EXISTS_ERROR = BASE_CONFIG_ERROR + 1;

    private ErrorCode() {

    }
}
