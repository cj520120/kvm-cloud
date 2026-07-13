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
public class StorageSupportCategoryUpdateRequest {
    private int storageId;
    private int supportCategory;

    public void validate() {
        if (storageId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的存储ID");
        }
        if (supportCategory < 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的支持类型");
        }
    }
}