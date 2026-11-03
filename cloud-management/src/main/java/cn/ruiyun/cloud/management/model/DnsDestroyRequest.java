package cn.ruiyun.cloud.management.model;

import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.util.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DnsDestroyRequest {
    private int dnsId;

    public void validate() {
        if (dnsId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的DNS ID");
        }
    }
}