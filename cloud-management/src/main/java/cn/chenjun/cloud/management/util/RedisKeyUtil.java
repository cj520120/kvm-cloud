package cn.chenjun.cloud.management.util;

/**
 * @author chenjun
 */
public class RedisKeyUtil {
    public final static String GLOBAL_NOTIFY_KET = "Cloud.Notify";
    public final static String GLOBAL_LOCK_KEY = "Cloud.Lock";
    public final static String OPERATE_TASK_KEY = "Cloud.Task";
    public final static String TASK_ID_SYNC_KEY = "Cloud.TaskId.Sync";
    public final static String HOST_SYNC_KEY = "Cloud.Host.Sync";
    public final static String HOST_GUEST_SYNC_KEY = "Cloud.Host.Guest.Sync";
    public final static String VOLUME_SYNC_KEY = "Cloud.Volume.Sync";
    public final static String STORAGE_SYNC_KEY = "Cloud.Storage.Sync";
    public final static String OPERATE_TASK_KEEP = "Cloud.Task.Keep.%s";
}
