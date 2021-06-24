package com.roamblue.cloud.management.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_vnc_info")
public class VncEntity {

    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    @TableField("vnc_host")
    private String vncHost;
    @TableField("vnc_port")
    private Integer vncPort;
    @TableField("vnc_password")
    private String vncPassword;
    @TableField("vm_id")
    private Integer vmId;
    @TableField("network_id")
    private Integer networkId;
    @TableField("cluster_id")
    private Integer clusterId;
}
