package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;

/**
 * @author chenjun
 */
public class TemplateScriptRequest {
    private int templateId;
    private String localCloudCfg;
    private String vendorData;
    private int cloudWaitFlag;

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public String getLocalCloudCfg() {
        return localCloudCfg;
    }

    public void setLocalCloudCfg(String localCloudCfg) {
        this.localCloudCfg = localCloudCfg;
    }

    public String getVendorData() {
        return vendorData;
    }

    public void setVendorData(String vendorData) {
        this.vendorData = vendorData;
    }

    public int getCloudWaitFlag() {
        return cloudWaitFlag;
    }

    public void setCloudWaitFlag(int cloudWaitFlag) {
        this.cloudWaitFlag = cloudWaitFlag;
    }

    public void validate() {
        if (templateId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的模板ID");
        }
    }
}