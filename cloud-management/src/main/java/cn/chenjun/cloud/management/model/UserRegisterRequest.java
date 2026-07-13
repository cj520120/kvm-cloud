package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import org.springframework.util.StringUtils;

/**
 * @author chenjun
 */
public class UserRegisterRequest {
    private String userName;
    private String loginName;
    private String password;
    private short userType;
    private short userStatus;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

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

    public short getUserType() {
        return userType;
    }

    public void setUserType(short userType) {
        this.userType = userType;
    }

    public short getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(short userStatus) {
        this.userStatus = userStatus;
    }

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