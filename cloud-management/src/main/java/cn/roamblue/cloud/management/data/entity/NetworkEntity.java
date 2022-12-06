package cn.roamblue.cloud.management.data.entity;

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
    @TableField("cluster_id")
    private int clusterId;
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
    @TableField("network_bridge_name")
    private String bridge;
    @TableField("network_dns")
    private String dns;
    @TableField("network_type")
    private String type;
    @TableField("network_status")
    private int status;
    @TableField("network_vlan_id")
    private int vlanId;
    @TableField("network_basic_network_id")
    private int basicNetworkId;
    @TableField("create_time")
    private Date createTime;
}
