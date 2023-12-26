package cn.chenjun.cloud.common.bean;

import cn.chenjun.cloud.common.util.Constant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基础网络信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasicBridgeNetwork {
    private String poolId;
    /**
     * 桥接名称
     */
    private String bridge;
    /**
     * 桥接方式
     */
    private  Constant.NetworkBridgeType bridgeType;
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
