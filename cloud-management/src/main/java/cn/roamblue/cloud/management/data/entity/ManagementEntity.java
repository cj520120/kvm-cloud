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
@TableName("tbl_management_info")
public class ManagementEntity {
    @TableId(type = IdType.ID_WORKER_STR)
    @TableField("server_id")
    private String serverId;
    @TableField("last_active_time")
    private Date lastActiveTime;
    @TableField("create_time")
    private Date createTime;
}
