package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

/**
 * @author chenjun
 */
@Setter
@Getter
public class SchemeModifyRequest {
    private int schemeId;
    private String name;
    private int cpu;
    private long memory;
    private int share;
    private int sockets;
    private int cores;
    private int threads;

    public void validate() {
        if (schemeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的方案ID");
        }
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入方案名称");
        }
        if (cpu <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "CPU数量必须大于0");
        }
        if (memory <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "内存大小必须大于0");
        }

    }
}