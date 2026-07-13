package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;

/**
 * @author chenjun
 */
public class UserDestroyRequest {
    private int userId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void validate() {
        if (userId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的用户ID");
        }
    }
}