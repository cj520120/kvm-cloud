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
    @TableField("id")
    private Integer id;
    @TableField("cluster_id")
    private Integer clusterId;
    @TableField("network_name")
    private String networkName;
    @TableField("network_subnet")
    private String networkSubnet;
    @TableField("network_gateway")
    private String networkGateway;
    @TableField("network_dns")
    private String networkDns;
    @TableField("network_manager_start_ip")
    private String networkManagerStartIp;
    @TableField("network_manager_end_ip")
    private String networkManagerEndIp;
    @TableField("network_guest_start_ip")
    private String networkGuestStartIp;
    @TableField("network_guest_end_ip")
    private String networkGuestEndIp;
    @TableField("network_card")
    private String networkCard;
    @TableField("network_type")
    private String networkType;
    @TableField("network_status")
    private String networkStatus;
    @TableField("create_time")
    private Date createTime;
}
