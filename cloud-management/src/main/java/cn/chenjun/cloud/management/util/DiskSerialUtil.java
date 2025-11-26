package cn.chenjun.cloud.management.util;

import java.text.SimpleDateFormat;

public class DiskSerialUtil {

    public static String generateDiskSerial() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "CJ" + sdf.format(System.currentTimeMillis());
    }
}
