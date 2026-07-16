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
public class UserSelfModifyRequest {
    private String username;
    private String oldPassword;
    private String newPassword;
    private String nonce;

    public void validate() {
        if (StringUtils.isEmpty(oldPassword)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入原密码");
        }
        if (StringUtils.isEmpty(newPassword)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入新密码");
        }
        if (StringUtils.isEmpty(nonce)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入随机数");
        }
    }
}