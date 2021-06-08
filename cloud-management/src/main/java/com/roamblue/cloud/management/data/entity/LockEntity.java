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
@TableName("tbl_lock_info")
public class LockEntity {


    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    @TableField("lock_name")
    private String lockName;
    @TableField("lock_uuid")
    private String lockUuid;
    @TableField("lock_thread")
    private Long lockThread;
    @TableField("lock_time")
    private Date lockTime;
    @TableField("lock_timeout")
    private Date lockTimeout;
}
