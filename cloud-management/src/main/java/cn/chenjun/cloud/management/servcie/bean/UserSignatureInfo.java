package cn.chenjun.cloud.management.servcie.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 签名数据
 *
 * @author chenjun
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserSignatureInfo {
    /**
     * 签名串
     */
    private String signature;
    /**
     * nonce
     */
    private String nonce;
}
