package com.roamblue.cloud.management.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_host_info")
public class HostEntity {

    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    @TableField("cluster_id")
    private Integer clusterId;
    @TableField("host_name")
    private String hostName;
    @TableField("host_ip")
    private String hostIp;
    @TableField("host_uri")
    private String hostUri;
    @TableField("host_status")
    private String hostStatus;
    @TableField("host_memory")
    private Long hostMemory;
    @TableField("host_cpu")
    private Integer hostCpu;
    @TableField("host_allocation_memory")
    private Long hostAllocationMemory;
    @TableField("host_allocation_cpu")
    private Integer hostAllocationCpu;
    @TableField("create_time")
    private Date createTime;
}
