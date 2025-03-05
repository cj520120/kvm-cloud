package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigModel {
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
