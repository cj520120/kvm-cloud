package cn.chenjun.cloud.management.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class KeepalivedConfigGenerator {

    private static final String CHARS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 生成 nodeid / router_id
     */
    public static String generateRouterId(String networkPoolId, int componentId) {
        try {
            String seed = networkPoolId + "|" + componentId;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(seed.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder("node_");
            for (int i = 0; i < 8; i++) {
                int idx = Math.abs(hash[i]) % CHARS.length();
                sb.append(CHARS.charAt(idx));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成 virtual_router_id (1~255)
     */
    public static int generateVrid(String networkPoolId, int componentId) {
        String seed = networkPoolId + "|" + componentId;
        int hash = seed.hashCode();
        return Math.abs(hash) % 255 + 1;
    }

    /**
     * 生成全局组播地址 224.0.0.x
     */
    public static String generateMcastGroup4(String networkPoolId, int componentId) {
        String seed = networkPoolId + "|" + componentId;
        int hash = seed.hashCode();
        int last = Math.abs(hash) % 253 + 2;
        return "224.0.0." + last;
    }

    /**
     * 生成 8 位认证密码
     */
    public static String generatePassword(String networkPoolId, int componentId) {
        try {
            String seed = networkPoolId + "|" + componentId;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(seed.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                int idx = Math.abs(hash[i]) % CHARS.length();
                sb.append(CHARS.charAt(idx));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        // 你的业务参数
        String networkPoolId = "12345678-1234-4321-abcd-abcdef123456";
        int componentId = 10;

        String routerId = generateRouterId(networkPoolId, componentId);
        int vrid = generateVrid(networkPoolId, componentId);
        String mcast = generateMcastGroup4(networkPoolId, componentId);
        String pass = generatePassword(networkPoolId, componentId);

        System.out.println("router_id       = " + routerId);
        System.out.println("vrid            = " + vrid);
        System.out.println("mcast_group4    = " + mcast);
        System.out.println("password        = " + pass);
    }
}