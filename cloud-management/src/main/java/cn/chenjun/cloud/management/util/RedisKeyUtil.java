package cn.chenjun.cloud.management.util;

/**
 * @author chenjun
 */
public class RedisKeyUtil {

    public static String getGlobalNotifyKey() {
        return "Cloud.Notify";
    }

    public static String getGlobalLockKey() {
        return "Cloud.Lock";
    }

    public static String getGlobalJobKey(String jobName) {
        return "Cloud.Job." + jobName;
    }

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
