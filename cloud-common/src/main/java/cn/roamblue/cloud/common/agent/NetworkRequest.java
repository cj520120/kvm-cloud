package cn.roamblue.cloud.common.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkRequest {
    /**
     * 命令
     */
    private String command;
    /**
     * 网路名称
     */
    private String name;
    /**
     * 网络类型 基础网络、Vlan网络
     */
    private String type;
    /**
     * 基础网络桥接信息
     */
    private BasicBridge basicBridge;
    /**
     * Vlan 网络信息
     */
    private Vlan vlan;

    /**
     * 基础网络信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BasicBridge{
        /**
         * 桥接名称
         */
        private String bridge;
        /**
         * 网卡名称
         */
        private String nic;
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

    }

    /**
     * Vlan网络
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Vlan{
        /**
         * vlan id 1-4096
         */
        private int vlanId;
        /**
         * vlan网卡名称
         */
        private String name;
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
    }
}
