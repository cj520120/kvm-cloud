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
public class NetworkNicAllocateRequest {
    private int guestNetworkId;
    private int allocateId;
    private String allocateDescription;

    public void validate() {
        if (guestNetworkId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的网卡ID");
        }
        if (StringUtils.isEmpty(allocateDescription)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入分配描述");
        }
    }
}