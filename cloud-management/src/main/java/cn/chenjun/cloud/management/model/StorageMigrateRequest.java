package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageMigrateRequest {
    private int sourceStorageId;
    private int destStorageId;

    public void validate() {
        if (sourceStorageId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的源存储ID");
        }
        if (destStorageId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的目标存储ID");
        }
        if (sourceStorageId == destStorageId) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "源存储和目标存储不能相同");
        }
    }
}