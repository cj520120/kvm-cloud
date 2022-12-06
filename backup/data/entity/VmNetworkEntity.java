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
@TableName("tbl_vm_network")
public class VmNetworkEntity {

    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    @TableField("network_id")
    private Integer networkId;
    @TableField("cluster_id")
    private Integer clusterId;
    @TableField("vm_id")
    private Integer vmId;
    @TableField("vm_device")
    private Integer vmDevice;
    @TableField("network_mac")
    private String networkMac;
    @TableField("network_ip")
    private String networkIp;
    @TableField("ip_type")
    private String ipType;
    @TableField("network_status")
    private String networkStatus;
    @TableField("create_time")
    private Date createTime;

}
