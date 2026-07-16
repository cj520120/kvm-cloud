package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chenjun
 */
@Setter
@Getter
public class TemplateScriptRequest {
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