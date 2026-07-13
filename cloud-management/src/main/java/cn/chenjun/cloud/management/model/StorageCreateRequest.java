package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageCreateRequest {
    private String description;
    private int supportCategory;
    private String type;
    private String param;

    public void validate() {
        if (StringUtils.isEmpty(description)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入存储描述");
        }
        if (supportCategory < 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的支持类型");
        }
        if (StringUtils.isEmpty(type)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入存储类型");
        }
        if (StringUtils.isEmpty(param)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入存储参数");
        }
    }
}