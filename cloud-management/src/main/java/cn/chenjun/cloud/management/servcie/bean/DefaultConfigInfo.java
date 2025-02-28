package cn.chenjun.cloud.management.servcie.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefaultConfigInfo {
    private String key;
    private Object value;
    private String description;
    private int allocateType;
    private int allocateId;
    private int valueType;
    private Object valueOptions;
}
