package cn.chenjun.cloud.management.util;

import java.util.UUID;

/**
 * @author chenjun
 */
public class NameUtil {
    private static final String[] KEY = new String[]{"l", "m", "x", "g", "h", "y", "H", "Y", "u", "v", "e", "C", "N", "X", "I", "c", "S", "J", "R", "p", "r", "E", "f", "O", "T", "P", "s", "B", "W", "K", "D", "t", "k", "i", "L", "V", "q", "j", "G", "d", "Q", "z", "w", "M", "b", "a", "U", "n", "o", "A", "F", "Z"};


    public static String generateGuestName() {
        long time = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        while (time != 0) {
            int index = (int) (time % KEY.length);
            sb.append(KEY[index]);
            time /= KEY.length;
        }
        sb.reverse();
        return "VM-" + sb;
    }

    public static String generateVolumeName() {
        return "VOL-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    public static String generateTemplateVolumeName() {
        return "TPL-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
