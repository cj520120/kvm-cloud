package cn.chenjun.cloud.management.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpValidator {
    private static final String IP_PATTERN =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final Pattern pattern = Pattern.compile(IP_PATTERN);

    public static boolean isValidIp(String ip) {
        if (ip == null) {
            return false;
        }
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }

}