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
public class GroupCreateRequest {
    private String groupName;

    public void validate() {
        if (StringUtils.isEmpty(groupName)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入分组名称");
        }
    }
}