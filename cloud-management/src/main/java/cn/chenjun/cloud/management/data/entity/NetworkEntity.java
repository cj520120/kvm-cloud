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

    @TableId(type = IdType.AUTO)
    @TableField("network_id")
    private Integer networkId;
    @TableField("network_name")
    private String name;
    @TableField("network_start_ip")
    private String startIp;
    @TableField("network_stop_ip")
    private String endIp;
    @TableField("network_gateway")
    private String gateway;
    @TableField("network_mask")
    private String mask;
    @TableField("network_subnet")
    private String subnet;
    @TableField("network_broadcast")
    private String broadcast;
    @TableField("network_bridge_name")
    private String bridge;
    @TableField("network_dns")
    private String dns;
    @TableField("network_type")
    private Integer type;
    @TableField("network_status")
    private Integer status;
    @TableField("network_vlan_id")
    private Integer vlanId;
    @TableField("network_secret")
    private String secret;
    @TableField("network_basic_network_id")
    private Integer basicNetworkId;
    @TableField("create_time")
    private Date createTime;
}
