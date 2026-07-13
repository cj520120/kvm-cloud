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
public class GuestReinstallRequest {
    private int guestId;
    private int category;
    private int bootstrapType;
    private String bootDeviceDriver;
    private int isoTemplateId;
    private int diskTemplateId;
    private int volumeId;
    private int storageId;
    private long diskSize;

    public void validate() {
        if (guestId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的虚拟机ID");
        }
        if (StringUtils.isEmpty(bootDeviceDriver)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择磁盘驱动");
        }
        if (isoTemplateId <= 0 && diskTemplateId <= 0 && volumeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择系统来源");
        }
        if (isoTemplateId > 0 && diskSize <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入磁盘大小");
        }
    }
}