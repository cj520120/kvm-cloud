package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleNetworkModel {

    private int networkId;
    private String poolId;
    private String name;
    private String startIp;
    private String endIp;
    private String gateway;
    private String mask;
    private String subnet;
    private String broadcast;
    private String bridge;
    private int bridgeType;
    private String dns;
    private String domain;
    private int type;
    private int status;
    private int vlanId;
    private int basicNetworkId;
    private Date createTime;
}
