package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SshDestroyRequest {
    private int id;

    public void validate() {
        if (id <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的密钥ID");
        }
    }
}