package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;

/**
 * @author chenjun
 */
public class HostUpdateRoleRequest {
    private int hostId;
    private int role;

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public void validate() {
        if (hostId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的主机ID");
        }
    }
}