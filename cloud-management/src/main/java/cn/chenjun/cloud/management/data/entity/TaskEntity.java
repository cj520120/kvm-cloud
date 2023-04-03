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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_task_info")
public class TaskEntity {
    @TableId(type = IdType.INPUT)
    @TableField("task_id")
    private String taskId;

    @TableField("task_version")
    private Integer version;
    @TableField("task_title")
    private String title;
    @TableField("task_type")
    private String type;
    @TableField("task_param")
    private String param;
    @TableField("create_time")
    private Date createTime;
    @TableField("expire_time")
    private Date expireTime;
}
