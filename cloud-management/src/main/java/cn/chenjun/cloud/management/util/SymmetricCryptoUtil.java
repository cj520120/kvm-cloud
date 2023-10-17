package cn.chenjun.cloud.management.util;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author gogo
 */
public class SymmetricCryptoUtil {
    private final Mode mode = Mode.CBC;
    private final Padding padding = Padding.ZeroPadding;
    /**
     * 16字节
     */
    private final String encodeKey;
    private final String ivKey;
    private SymmetricCryptoUtil(String encodeKey, String ivKey) {
        this.encodeKey = encodeKey;
        this.ivKey = ivKey;
    }
    private SymmetricCryptoUtil(){
        this.encodeKey=ThreadLocalRandom.current().nextLong(1000000000000000L, 9999999999999999L) + "";
        this.ivKey=ThreadLocalRandom.current().nextLong(1000000000000000L, 9999999999999999L) + "";
    }

    public String getEncodeKey() {
        return encodeKey;
    }

    public String getIvKey() {
        return ivKey;
    }

    public static SymmetricCryptoUtil build(String encodeKey, String ivKey){
        return new SymmetricCryptoUtil(encodeKey,ivKey);
    }
    public static SymmetricCryptoUtil build(){
        return new SymmetricCryptoUtil();
    }




    public String encrypt(String data ) {
        AES aes;
        if (Mode.CBC == mode) {
            aes = new AES(mode, padding,
                    new SecretKeySpec(encodeKey.getBytes(), "AES"),
                    new IvParameterSpec(ivKey.getBytes()));
        } else {
            aes = new AES(mode, padding,
                    new SecretKeySpec(encodeKey.getBytes(), "AES"));
        }
        return aes.encryptBase64(data, StandardCharsets.UTF_8);
    }

    public String decrypt(String data ) {
        AES aes;
        if (Mode.CBC == mode) {
            aes = new AES(mode, padding,
                    new SecretKeySpec(encodeKey.getBytes(), "AES"),
                    new IvParameterSpec(ivKey.getBytes()));
        } else {
            aes = new AES(mode, padding,
                    new SecretKeySpec(encodeKey.getBytes(), "AES"));
        }
        byte[] decryptDataBase64 = aes.decrypt(data);
        return new String(decryptDataBase64, StandardCharsets.UTF_8);
    }

}

