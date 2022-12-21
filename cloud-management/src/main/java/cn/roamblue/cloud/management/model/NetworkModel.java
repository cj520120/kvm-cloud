package cn.roamblue.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NetworkModel {

    private int networkId;
    private String name;
    private String startIp;
    private String endIp;
    private String gateway;
    private String mask;
    private String bridge;
    private String dns;
    private int type;
    private int status;
    private int vlanId;
    private int basicNetworkId;
    private Date createTime;
}
