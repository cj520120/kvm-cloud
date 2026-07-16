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
public class UserUpdateRequest {
    private int userId;
    private String userName;
    private short userType;
    private short userStatus;

    public void validate() {
        if (userId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的用户ID");
        }
        if (StringUtils.isEmpty(userName)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入用户名");
        }
    }
}