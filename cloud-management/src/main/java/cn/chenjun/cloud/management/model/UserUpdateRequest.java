package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import org.springframework.util.StringUtils;

/**
 * @author chenjun
 */
public class UserUpdateRequest {
    private int userId;
    private String userName;
    private short userType;
    private short userStatus;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
        if (userId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的用户ID");
        }
        if (StringUtils.isEmpty(userName)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入用户名");
        }
    }
}