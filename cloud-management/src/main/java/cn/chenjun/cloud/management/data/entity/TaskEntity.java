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

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_task_info")
public class TaskEntity {
    public static final String TASK_ID = "task_id";
    public static final String TASK_VERSION = "task_version";
    public static final String TASK_TITLE = "task_title";
    public static final String TASK_TYPE = "task_type";
    public static final String TASK_PARAM = "task_param";
    public static final String CREATE_TIME = "create_time";
    public static final String EXPIRE_TIME = "expire_time";

    @TableId(type = IdType.INPUT)
    @TableField(TASK_ID)
    private String taskId;
    @TableField(TASK_VERSION)
    private Integer version;
    @TableField(TASK_TITLE)
    private String title;
    @TableField(TASK_TYPE)
    private String type;
    @TableField(TASK_PARAM)
    private String param;
    @TableField(CREATE_TIME)
    private Date createTime;
    @TableField(EXPIRE_TIME)
    private Date expireTime;
}