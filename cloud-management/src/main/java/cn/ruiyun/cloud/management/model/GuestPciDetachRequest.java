package cn.ruiyun.cloud.management.model;

import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.util.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GuestPciDetachRequest {
    private int id;

    public void validate() {
        if (id <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择设备");
        }
    }
}
