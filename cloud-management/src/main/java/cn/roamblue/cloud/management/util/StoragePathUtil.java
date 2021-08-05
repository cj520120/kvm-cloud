package cn.roamblue.cloud.management.util;

import java.nio.file.Paths;

/**
 * @author chenjun
 * @ClassName: StorageVolUtil
 * @Description: TODO
 * @Create by: chenjun
 * @Date: 2021/8/5 上午10:58
 */
public class StoragePathUtil {
    public static String getVolumePath(String storageName, String volName){
        return Paths.get("/mnt",storageName,volName).toString();
    }
    public static String getMountPath(String storageName){
        return Paths.get("/mnt",storageName).toString();
    }
}
