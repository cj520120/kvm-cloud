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
public class TemplateCreateRequest {
    private String name;
    private String uri;
    private String md5;
    private int templateType;
    private String arch;
    private String localCloudCfg;
    private String vendorData;
    private int cloudWaitFlag;

    public void validate() {
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入模板名称");
        }
        if (StringUtils.isEmpty(uri)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入模板URI");
        }
        if (templateType < 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的模板类型");
        }
        if (StringUtils.isEmpty(arch)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入架构类型");
        }
    }
}