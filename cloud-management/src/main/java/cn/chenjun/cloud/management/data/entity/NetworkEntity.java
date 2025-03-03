package cn.chenjun.cloud.management.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("tbl_network_info")
public class NetworkEntity {
    public static final String NETWORK_ID = "network_id";
    public static final String NETWORK_POOL_ID = "network_pool_id";
    public static final String NETWORK_NAME = "network_name";
    public static final String NETWORK_START_IP = "network_start_ip";
    public static final String NETWORK_STOP_IP = "network_stop_ip";
    public static final String NETWORK_GATEWAY = "network_gateway";
    public static final String NETWORK_MASK = "network_mask";
    public static final String NETWORK_SUBNET = "network_subnet";
    public static final String NETWORK_BROADCAST = "network_broadcast";
    public static final String NETWORK_BRIDGE_NAME = "network_bridge_name";
    public static final String NETWORK_DNS = "network_dns";
    public static final String NETWORK_TYPE = "network_type";
    public static final String NETWORK_STATUS = "network_status";
    public static final String NETWORK_VLAN_ID = "network_vlan_id";
    public static final String NETWORK_SECRET = "network_secret";
    public static final String NETWORK_DOMAIN = "network_domain";
    public static final String NETWORK_BRIDGE_TYPE = "network_bridge_type";
    public static final String NETWORK_BASIC_NETWORK_ID = "network_basic_network_id";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO,value = NETWORK_ID)
    private Integer networkId;
    @TableField(NETWORK_POOL_ID)
    private String poolId;
    @TableField(NETWORK_NAME)
    private String name;
    @TableField(NETWORK_START_IP)
    private String startIp;
    @TableField(NETWORK_STOP_IP)
    private String endIp;
    @TableField(NETWORK_GATEWAY)
    private String gateway;
    @TableField(NETWORK_MASK)
    private String mask;
    @TableField(NETWORK_SUBNET)
    private String subnet;
    @TableField(NETWORK_BROADCAST)
    private String broadcast;
    @TableField(NETWORK_BRIDGE_NAME)
    private String bridge;
    @TableField(NETWORK_BRIDGE_TYPE)
    private Integer bridgeType;
    @TableField(NETWORK_DNS)
    private String dns;
    @TableField(NETWORK_TYPE)
    private Integer type;
    @TableField(NETWORK_STATUS)
    private Integer status;
    @TableField(NETWORK_VLAN_ID)
    private Integer vlanId;
    @TableField(NETWORK_SECRET)
    private String secret;
    @TableField(NETWORK_DOMAIN)
    private String domain;
    @TableField(NETWORK_BASIC_NETWORK_ID)
    private Integer basicNetworkId;
    @TableField(CREATE_TIME)
    private Date createTime;
}