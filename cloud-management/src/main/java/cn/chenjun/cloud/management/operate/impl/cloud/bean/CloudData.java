package cn.chenjun.cloud.management.operate.impl.cloud.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloudData {
    private String data;
    private boolean waiting;
}
