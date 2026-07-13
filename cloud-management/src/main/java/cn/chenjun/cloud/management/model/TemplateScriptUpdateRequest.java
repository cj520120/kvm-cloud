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
public class TemplateScriptUpdateRequest {
    private int templateId;
    private String localCloudCfg;
    private String vendorData;
    private int cloudWaitFlag;

    public void validate() {
        if (templateId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的模板ID");
        }
    }
}