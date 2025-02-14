package cn.chenjun.cloud.management.servcie.bean;

import cn.chenjun.cloud.management.util.MetaDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaData {
    private MetaDataType type;
    private String body;
}
