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
    @TableField("component_vip")
    private String componentVip;
    @TableField("network_id")
    private Integer networkId;
    @TableField("master_guest_id")
    private Integer masterGuestId;
    @TableField("component_slave_number")
    private Integer componentSlaveNumber;
    @TableField("slave_guest_ids")
    private String slaveGuestIds;
}
