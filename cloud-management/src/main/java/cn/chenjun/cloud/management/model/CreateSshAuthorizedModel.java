package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateSshAuthorizedModel {
    private Integer id;
    private String name;
    private String publicKey;
    private String privateKey;
}
