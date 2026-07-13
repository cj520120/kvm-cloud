package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import org.springframework.util.StringUtils;

/**
 * @author chenjun
 */
public class LoginRequest {
    private String loginName;
    private String password;
    private String nonce;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

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