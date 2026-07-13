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
public class ConfigUpdateRequest {
    private String configKey;
    private int allocateType;
    private int allocateId;
    private String configValue;

    public void validate() {
        if (StringUtils.isEmpty(configKey)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入配置键");
        }
        if (allocateType < 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的分配类型");
        }
        if (allocateId < 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的分配ID");
        }
        if (StringUtils.isEmpty(configValue)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入配置值");
        }
    }
}