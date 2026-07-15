package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestPciAttachRequest {

    // PCI 十六进制正则：仅允许 0-9、a-f、A-F
    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");

    private int guestId;
    // 改为字符串，存储十六进制
    private String domain;
    private String bus;
    private String slot;
    private String func;
    private String description;

    public void validate() {
        if (guestId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择虚拟机");
        }

        // 校验 domain：4位十六进制
        if (!StringUtils.hasText(domain) || domain.length() != 4 || !HEX_PATTERN.matcher(domain).matches()) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "域必须为4位十六进制字符");
        }

        // 校验 bus：2位十六进制
        if (!StringUtils.hasText(bus) || bus.length() != 2 || !HEX_PATTERN.matcher(bus).matches()) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "总线必须为2位十六进制字符");
        }

        // 校验 slot：2位十六进制
        if (!StringUtils.hasText(slot) || slot.length() != 2 || !HEX_PATTERN.matcher(slot).matches()) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "插槽必须为2位十六进制字符");
        }

        // 校验 func：1位十六进制（PCI function 范围 0~7）
        if (!StringUtils.hasText(func) || func.length() != 1 || !HEX_PATTERN.matcher(func).matches()) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "功能号必须为1位十六进制字符");
        }

        // 额外限制 function 合法范围 0-7
        char funcChar = func.charAt(0);
        if (funcChar < '0' || funcChar > '7') {
            throw new CodeException(ErrorCode.PARAM_ERROR, "功能号取值范围为 0~7");
        }

        if (!StringUtils.hasText(description)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的描述");
        }
    }
}