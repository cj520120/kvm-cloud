package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import org.springframework.util.StringUtils;

/**
 * @author chenjun
 */
public class UserSelfModifyRequest {
    private String username;
    private String oldPassword;
    private String newPassword;
    private String nonce;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

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