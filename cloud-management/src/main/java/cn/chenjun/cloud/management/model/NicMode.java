package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NicMode {
    private int id;
    private String mac;
    private String ip;
    private int allocateId;
    private int allocateType;
    private String allocateDescription;
}
