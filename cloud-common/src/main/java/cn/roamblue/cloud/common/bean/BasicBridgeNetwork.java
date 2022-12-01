package cn.roamblue.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基础网络信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasicBridgeNetwork {
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
