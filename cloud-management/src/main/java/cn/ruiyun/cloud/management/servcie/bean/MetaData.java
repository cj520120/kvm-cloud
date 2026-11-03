package cn.ruiyun.cloud.management.servcie.bean;

import cn.ruiyun.cloud.management.util.MetaDataType;
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
