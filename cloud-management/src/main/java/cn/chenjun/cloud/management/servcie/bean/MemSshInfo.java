package cn.chenjun.cloud.management.servcie.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MemSshInfo {
    private int id;
    private String name;
    private String publicKey;
    private String privateKey;
}
