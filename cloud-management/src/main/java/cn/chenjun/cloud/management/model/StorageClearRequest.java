package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;

/**
 * @author chenjun
 */
public class StorageClearRequest {
    private int storageId;

    public int getStorageId() {
        return storageId;
    }

    public void setStorageId(int storageId) {
        this.storageId = storageId;
    }

    public void validate() {
        if (storageId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的存储ID");
        }
    }
}