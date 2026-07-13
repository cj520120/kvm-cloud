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
public class SchemeCreateRequest {
    private String name;
    private int cpu;
    private long memory;
    private int share;
    private int sockets;
    private int cores;
    private int threads;

    public void validate() {
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入方案名称");
        }
        if (cpu <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的CPU核数");
        }
        if (memory <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的内存大小");
        }
    }
}