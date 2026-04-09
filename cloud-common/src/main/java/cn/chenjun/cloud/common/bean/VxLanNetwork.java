package cn.chenjun.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vlan网络
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VxLanNetwork {
    private String poolId;

    private String xml;

    /**
     * 基础网络
     */
    private BasicBridgeNetwork basic;

    private String bridge;
    private String localIp;
    private String remoteIps;
    private int vni;
}
