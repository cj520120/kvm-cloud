package cn.ruiyun.cloud.management.model;

import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.util.ErrorCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chenjun
 */
@Setter
@Getter
public class StorageClearRequest {
    private int storageId;

    public void validate() {
        if (storageId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的存储ID");
        }
    }
}