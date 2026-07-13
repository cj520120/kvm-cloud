package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SshImportRequest {
    private String name;
    private String publicKey;
    private String privateKey;

    public void validate() {
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入密钥名称");
        }
        if (StringUtils.isEmpty(publicKey)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入公钥");
        }
        if (StringUtils.isEmpty(privateKey)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入私钥");
        }
    }
}