package cn.ruiyun.cloud.management.util;

/**
 * @author chenjun
 */
public class CacheKeyUtil {


    public static String getGlobalLockKey() {
        return "Cloud.Lock";
    }



    public static String getHostLastKeepKey(int hostId) {
        return "Cloud.Host.Keep." + hostId;
    }

    public static String getHostConnectionKey(int hostId) {
        return "Cloud.Host.Connection." + hostId;
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

    public static String getGuestGraphicsToken(String token) {
        return "Guest.Graphics." + token;
    }

    public static String getSshDownloadKey(String token) {
        return "SSH.Download." + token;
    }
}
