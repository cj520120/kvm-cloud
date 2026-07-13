package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeBatchDestroyRequest {
    private List<Integer> volumeIds;

    public void validate() {
        if (volumeIds == null || volumeIds.isEmpty()) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择磁盘");
        }
        for (Integer volumeId : volumeIds) {
            if (volumeId <= 0) {
                throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的磁盘ID");
            }
        }
    }
}