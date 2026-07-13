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
public class VolumeMigrateRequest {
    private int sourceVolumeId;
    private int storageId;

    public void validate() {
        if (sourceVolumeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的源磁盘ID");
        }
        if (storageId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择存储池");
        }
    }
}