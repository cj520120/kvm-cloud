package cn.ruiyun.cloud.management.model;

import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.util.ErrorCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

/**
 * @author chenjun
 */
@Setter
@Getter
public class UserResetPasswordRequest {
    private int userId;
    private String password;

    public void validate() {
        if (userId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的用户ID");
        }
        if (StringUtils.isEmpty(password)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入密码");
        }
    }
}