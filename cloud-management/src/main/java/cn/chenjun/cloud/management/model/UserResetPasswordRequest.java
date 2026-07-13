package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import org.springframework.util.StringUtils;

/**
 * @author chenjun
 */
public class UserResetPasswordRequest {
    private int userId;
    private String password;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void validate() {
        if (userId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的用户ID");
        }
        if (StringUtils.isEmpty(password)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入密码");
        }
    }
}