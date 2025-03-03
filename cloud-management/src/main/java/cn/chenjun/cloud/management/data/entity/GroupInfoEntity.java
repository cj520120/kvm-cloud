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
@TableName("tbl_group_info")
public class GroupInfoEntity {
    public static final String GROUP_ID = "group_id";
    public static final String GROUP_NAME = "group_name";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO,value = GROUP_ID)
    private Integer groupId;

    @TableField(GROUP_NAME)
    private String groupName;

    @TableField(CREATE_TIME)
    private Date createTime;
}