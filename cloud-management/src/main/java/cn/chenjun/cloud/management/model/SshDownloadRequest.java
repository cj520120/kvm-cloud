package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;

/**
 * @author chenjun
 */
public class SshDownloadRequest {
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void validate() {
        if (id <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的SSH密钥ID");
        }
    }
}