package cn.chenjun.cloud.common.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * @author chenjun
 */
@Slf4j
public class AppUtils {

    private static final String HASH_ALGORITHM = "HmacSHA256";
    /**
     * app_secret 密钥
     */
    private final static String SERVER_NAME = "GMKJ:GAME:API";
    private final static String[] CHARS = new String[]{"a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z"};

    /**
     * 短8位UUID思想其实借鉴微博短域名的生成方式，但是其重复概率过高，而且每次生成4个，需要随即选取一个。
     * 本算法利用62个可打印字符，通过随机生成32位UUID，由于UUID都为十六进制，所以将UUID分成8组，每4个为一组，然后通过模62操作，结果作为索引取出字符，
     * 这样重复率大大降低。
     * 经测试，在生成一千万个数据也没有出现重复，完全满足大部分需求。
     * </p>
     *
     * @author mazhq
     * @date 2019/8/27 16:16
     */
    public static String getAppId() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(CHARS[x % 0x3E]);
        }
        return shortBuffer.toString();

    }

    /**
     * <p>
     * 通过appId和内置关键词生成APP Secret
     * </P>
     *
     * @author mazhq
     * @date 2019/8/27 16:32
     */
    public static String getAppSecret(String appId) {
        try {
            String[] array = new String[]{appId, SERVER_NAME};
            StringBuffer sb = new StringBuffer();
            // 字符串排序
            Arrays.sort(array);
            for (String s : array) {
                sb.append(s);
            }
            String str = sb.toString();
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(str.getBytes());
            byte[] digest = md.digest();

            StringBuffer hexstr = new StringBuffer();
            String shaHex;
            for (byte b : digest) {
                shaHex = Integer.toHexString(b & 0xFF);
                if (shaHex.length() < 2) {
                    hexstr.append(0);
                }
                hexstr.append(shaHex);
            }
            return hexstr.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }


    public static String sign(Map<String, Object> map, String appId, String appSecret, String nonce) throws InvalidKeyException, NoSuchAlgorithmException {
        StringBuilder sb = new StringBuilder();
        map.put("appId", appId);
        map.put("nonce", nonce);
        map.keySet().stream().sorted(String::compareTo).forEach(key -> sb.append(key).append("=").append(map.get(key)).append("&"));
        sb.deleteCharAt(sb.length() - 1);
        Key secretKey = new SecretKeySpec(appSecret.getBytes(), HASH_ALGORITHM);
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);
        final byte[] bytes = mac.doFinal(sb.toString().getBytes());
        sb.delete(0, sb.length());
        for (byte item : bytes) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100), 1, 3);

        }
        return sb.toString();
    }

}
