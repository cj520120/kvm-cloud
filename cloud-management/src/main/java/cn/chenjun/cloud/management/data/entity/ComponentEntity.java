package cn.chenjun.cloud.management.data.entity;

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
@TableName("tbl_component_info")
public class ComponentEntity {

    @TableId(type = IdType.AUTO)
    @TableField("component_id")
    private Integer componentId;
    @TableField("component_type")
    private Integer componentType;
    @TableField("network_id")
    private Integer networkId;
    @TableField("guest_id")
    private Integer guestId;
}
