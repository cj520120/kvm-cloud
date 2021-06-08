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
@TableName("tbl_task_info")
public class ManagementTaskEntity {
    @TableId(type = IdType.ID_WORKER_STR)
    @TableField("task_name")
    private String taskName;
    @TableField("server_id")
    private String serverId;
    @TableField("create_time")
    private Date createTime;
}
