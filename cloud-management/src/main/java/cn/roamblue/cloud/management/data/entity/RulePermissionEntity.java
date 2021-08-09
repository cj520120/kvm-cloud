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
@TableName("tbl_rule_permission")
public class RulePermissionEntity {
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    @TableField("group_name")
    private String groupName;
    @TableField("group_permissions")
    private String groupPermissions;

}
