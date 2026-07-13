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
public class HostCreateRequest {
    private String displayName;
    private String hostIp;
    private String uri;
    private String nic;
    private int role;

    public void validate() {
        if (StringUtils.isEmpty(displayName)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入主机名称");
        }
        if (StringUtils.isEmpty(hostIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入主机IP");
        }
        if (StringUtils.isEmpty(uri)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入主机URI");
        }
        if (StringUtils.isEmpty(nic)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入网卡名称");
        }
        if (role < 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的角色");
        }
    }
}