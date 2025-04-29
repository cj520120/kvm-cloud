package cn.chenjun.cloud.management.util;

/**
 * @author chenjun
 */
public class RedisKeyUtil {
    public static final String GLOBAL_NOTIFY_KET = "Cloud.Notify";
    public static final String GLOBAL_LOCK_KEY = "Cloud.Lock";
    public static final String JOB_RUN_TIME = "Cloud.Job.";

    public static String getHostLastKeepKey(int hostId) {
        return "Cloud.Host.Keep." + hostId;
    }

    public static String getUserToken(Integer userId) {
        return "User.Token." + userId;
    }

    public static String getTokenUser(String token) {
        return "User.Token." + token;
    }

    public static String getUserInfo(Integer userId) {
        return "User.Info." + userId;
    }
}
