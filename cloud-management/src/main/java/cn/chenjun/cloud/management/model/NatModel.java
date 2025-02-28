package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NatModel {
    private int natId;
    private int componentId;
    private String protocol;
    private int localPort;
    private String remoteIp;
    private int remotePort;

}
