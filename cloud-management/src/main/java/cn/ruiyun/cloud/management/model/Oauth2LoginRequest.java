package cn.ruiyun.cloud.management.model;

import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.util.ErrorCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

/**
 * @author chenjun
 */
@Setter
@Getter
public class Oauth2LoginRequest {
    private String code;

    public void validate() {
        if (StringUtils.isEmpty(code)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入授权码");
        }
    }
}