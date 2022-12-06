package cn.roamblue.cloud.management.util;

/**
 * @author chenjun
 * @ClassName: StorageVolUtil
 * @Description: TODO
 * @Create by: chenjun
 * @Date: 2021/8/5 上午10:58
 */
public class StoragePathUtil {
    public static String getVolumePath(String storageName, String volName) {
        return "/mnt/" + storageName + "/" + volName;
    }

    public static String getMountPath(String storageName) {
        return "/mnt/" + storageName;
    }
}
