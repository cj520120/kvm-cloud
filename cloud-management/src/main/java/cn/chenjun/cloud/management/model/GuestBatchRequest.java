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
public class GuestBatchRequest {
    private List<Integer> guestIds;
    private boolean force;

    public void validate() {
        if (guestIds == null || guestIds.isEmpty()) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择虚拟机");
        }
        for (Integer guestId : guestIds) {
            if (guestId <= 0) {
                throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的虚拟机ID");
            }
        }
    }
}