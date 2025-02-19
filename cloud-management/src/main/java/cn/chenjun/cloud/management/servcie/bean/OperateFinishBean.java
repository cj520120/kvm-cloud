package cn.chenjun.cloud.management.servcie.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperateFinishBean<T> {
    private String taskId;
    private String operateType;
    private String param;
    private String result;
}
