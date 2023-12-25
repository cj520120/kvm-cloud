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
public class VlanNetwork {
    private int id;
    /**
     * vlan id 1-4096
     */
    private int vlanId;
    /**
     * vlan 桥接网卡名称
     */
    private String bridge;
    /**
     * IP
     */
    private String ip;
    /**
     * 子网
     */
    private String netmask;
    /**
     * 网关
     */
    private String geteway;

    /**
     * 基础网络
     */
    private BasicBridgeNetwork basic;
}
