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
public class GuestDiskCreateRequest {
    private int guestId;
    private String diskDriver;
    private String description;
    private int storageId;
    private long volumeSize;

    public void validate() {
        if (guestId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的虚拟机ID");
        }
        if (StringUtils.isEmpty(diskDriver)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择磁盘驱动");
        }
        if (StringUtils.isEmpty(description)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入磁盘描述");
        }
        if (volumeSize <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入磁盘大小");
        }
    }
}