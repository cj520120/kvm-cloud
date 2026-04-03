package cn.chenjun.cloud.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SecurityUtil {
    public static String signature(Map<String, Object> headers, String secret) {
        List<String> signatureDataList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            signatureDataList.add(key + ":" + value);
        }
        Collections.sort(signatureDataList);
        String signatureStr = String.join("&", signatureDataList) + secret;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md.digest(signatureStr.getBytes());
            StringBuilder hexBuilder = new StringBuilder();
            for (byte b : md5Bytes) {
                hexBuilder.append(String.format("%02x", b));
            }
            return hexBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

}
