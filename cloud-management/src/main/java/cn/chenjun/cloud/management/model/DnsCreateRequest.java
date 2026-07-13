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
public class DnsCreateRequest {
    private int networkId;
    private String domain;
    private String ip;

    public void validate() {
        if (networkId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的网络ID");
        }
        if (StringUtils.isEmpty(domain)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入域名");
        }
        if (StringUtils.isEmpty(ip)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入IP地址");
        }
    }
}