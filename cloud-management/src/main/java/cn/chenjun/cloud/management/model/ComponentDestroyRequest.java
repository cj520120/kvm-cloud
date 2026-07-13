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
public class ComponentDestroyRequest {
    private int componentId;

    public void validate() {
        if (componentId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的组件ID");
        }
    }
}