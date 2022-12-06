package cn.roamblue.cloud.management.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_permission_info")
public class PermissionInfoEntity {
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    @TableField("permission_name")
    private String permissionName;
    @TableField("permission_description")
    private String permissionDescription;
    @TableField("category_id")
    private Integer categoryId;
    @TableField("permission_sort")
    private Integer permissionSort;

}
