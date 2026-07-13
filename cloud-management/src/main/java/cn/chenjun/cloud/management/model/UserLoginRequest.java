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
public class UserLoginRequest {
    private String loginName;
    private String password;
    private String nonce;

    public void validate() {
        if (StringUtils.isEmpty(loginName)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入登录名");
        }
        if (StringUtils.isEmpty(password)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入密码");
        }
        if (StringUtils.isEmpty(nonce)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入随机数");
        }
    }
}