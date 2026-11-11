package cn.ruiyun.cloud.management.util;

import java.text.SimpleDateFormat;

public class DiskSerialUtil {

    public static String generateDiskSerial() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "RY" + sdf.format(System.currentTimeMillis());
    }
}
