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
public class NatCreateRequest {
    private int componentId;
    private int localPort;
    private String protocol;
    private String remoteIp;
    private int remotePort;

    public void validate() {
        if (componentId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的组件ID");
        }
        if (localPort <= 0 || localPort > 65535) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的本地端口(1-65535)");
        }
        if (StringUtils.isEmpty(protocol)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入协议类型");
        }
        if (StringUtils.isEmpty(remoteIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入远程IP");
        }
        if (remotePort <= 0 || remotePort > 65535) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的远程端口(1-65535)");
        }
    }
}