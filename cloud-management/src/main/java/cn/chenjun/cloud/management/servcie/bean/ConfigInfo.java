package cn.chenjun.cloud.management.servcie.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfigInfo {
    private int id;
    private String key;
    private Object value;
    private int allocateType;
    private int allocateId;
    private String description;
    private int valueType;
    private Object valueOptions;
    private boolean intern;
    private boolean defaultParam;
}
