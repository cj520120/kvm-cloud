package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chenjun
 */
@Setter
@Getter
public class HostUpdateRoleRequest {
    private int hostId;
    private int role;

    public void validate() {
        if (hostId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的主机ID");
        }
    }
}