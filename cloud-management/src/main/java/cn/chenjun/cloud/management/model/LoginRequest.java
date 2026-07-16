package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

/**
 * @author chenjun
 */
@Setter
@Getter
public class LoginRequest {
    private String loginName;
    private String password;
    private String nonce;

    public void validate() {
        if (StringUtils.isEmpty(loginName)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入用户名");
        }
        if (StringUtils.isEmpty(password)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入密码");
        }
        if (StringUtils.isEmpty(nonce)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入随机数");
        }
    }
}