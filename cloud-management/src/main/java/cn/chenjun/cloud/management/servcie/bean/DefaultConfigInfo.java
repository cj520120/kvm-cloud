package cn.chenjun.cloud.management.servcie.bean;

import cn.chenjun.cloud.management.servcie.convert.ConfigConvert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefaultConfigInfo<T> {
    private String key;
    private T value;
    private String description;
    private int allocateType;
    private int allocateId;
    private int valueType;
    private Object valueOptions;
    private ConfigConvert<T> convert;
}
