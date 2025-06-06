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
@TableName("tbl_component_info")
public class ComponentEntity {
    public static final String COMPONENT_ID = "component_id";
    public static final String COMPONENT_TYPE = "component_type";
    public static final String COMPONENT_VIP = "component_vip";
    public static final String BASIC_COMPONENT_VIP = "basic_component_vip";
    public static final String NETWORK_ID = "network_id";
    public static final String MASTER_GUEST_ID = "master_guest_id";
    public static final String COMPONENT_SLAVE_NUMBER = "component_slave_number";
    public static final String SLAVE_GUEST_IDS = "slave_guest_ids";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO, value = COMPONENT_ID)
    private Integer componentId;
    @TableField(COMPONENT_TYPE)
    private Integer componentType;
    @TableField(COMPONENT_VIP)
    private String componentVip;
    @TableField(BASIC_COMPONENT_VIP)
    private String basicComponentVip;
    @TableField(NETWORK_ID)
    private Integer networkId;
    @TableField(MASTER_GUEST_ID)
    private Integer masterGuestId;
    @TableField(COMPONENT_SLAVE_NUMBER)
    private Integer componentSlaveNumber;
    @TableField(SLAVE_GUEST_IDS)
    private String slaveGuestIds;
    @TableField(CREATE_TIME)
    private Date createTime;
}