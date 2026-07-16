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
public class UserRegisterRequest {
    private String userName;
    private String loginName;
    private String password;
    private short userType;
    private short userStatus;

    public void validate() {
        if (StringUtils.isEmpty(userName)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入用户名");
        }
        if (StringUtils.isEmpty(loginName)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入登录名");
        }
        if (StringUtils.isEmpty(password)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入密码");
        }
    }
}