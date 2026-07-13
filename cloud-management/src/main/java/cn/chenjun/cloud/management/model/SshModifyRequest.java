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
public class SshModifyRequest {
    private int id;
    private String name;

    public void validate() {
        if (id <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的密钥ID");
        }
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入密钥名称");
        }
    }
}