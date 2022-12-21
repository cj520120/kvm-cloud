package cn.roamblue.cloud.management.util;

public class GuestNameUtil {
    private static final String[] KEY = new String[]{"l", "m", "x", "g", "h", "y", "H", "Y", "u", "v", "e", "C", "N", "X", "I", "c", "S", "J", "R", "p", "r", "E", "f", "O", "T", "P", "s", "B", "W", "K", "D", "t", "k", "i", "L", "V", "q", "j", "G", "d", "Q", "z", "w", "M", "b", "a", "U", "n", "o", "A", "F", "Z"};


    public static String getName() {
        long time = System.nanoTime();
        StringBuffer sb = new StringBuffer();
        while (time != 0) {
            int index = (int) (time % KEY.length);
            sb.append(KEY[index]);
            time /= KEY.length;
        }
        sb.reverse();
        return "VM-" + sb;
    }

}
