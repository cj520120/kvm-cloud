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
@TableName("tbl_vm_statics")
public class VmStaticsEntity {

    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    @TableField("vm_id")
    private Integer vmId;
    @TableField("disk_write_speed")
    private Long diskWriteSpeed;
    @TableField("disk_read_speed")
    private Long diskReadSpeed;
    @TableField("network_send_speed")
    private Long networkSendSpeed;
    @TableField("network_receive_speed")
    private Long networkReceiveSpeed;
    @TableField("cpu_usage")
    private int cpuUsage;
    @TableField("create_time")
    private Date createTime;


}
