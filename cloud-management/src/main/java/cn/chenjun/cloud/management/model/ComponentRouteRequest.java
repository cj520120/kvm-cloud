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
public class ComponentRouteRequest {
    private int componentId;
    private String destIp;
    private int cidr;
    private String nexthop;

    public void validate() {
        if (componentId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的组件ID");
        }
        if (StringUtils.isEmpty(destIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入目标IP");
        }
        if (cidr <= 0 || cidr > 32) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的子网掩码(1-32)");
        }
        if (StringUtils.isEmpty(nexthop)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入下一跳IP");
        }
    }
}