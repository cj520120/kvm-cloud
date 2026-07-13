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
public class VolumeTemplateCreateRequest {
    private int volumeId;
    private String name;
    private String arch;

    public void validate() {
        if (volumeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的磁盘ID");
        }
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入模板名称");
        }
        if (StringUtils.isEmpty(arch)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入架构类型");
        }
    }
}