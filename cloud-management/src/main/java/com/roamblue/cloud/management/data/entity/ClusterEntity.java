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
@TableName("tbl_cluster_info")
public class ClusterEntity {
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    @TableField("cluster_name")
    private String clusterName;
    @TableField("cluster_status")
    private String clusterStatus;
    @TableField("over_cpu")
    private Float overCpu;
    @TableField("over_memory")
    private Float overMemory;
    @TableField("create_time")
    private Date createTime;

}
