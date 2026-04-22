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
@TableName("tbl_route_strategy")
public class RouteStrategyEntity {

    // 字段常量（和你风格保持一致）
    public static final String ID = "id";
    public static final String COMPONENT_ID = "component_id";
    public static final String DEST_IP = "dest_ip";
    public static final String CIDR = "cidr";
    public static final String NEXTHOP = "nexthop";
    public static final String CREATE_TIME = "create_time";

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO, value = ID)
    private Integer id;

    /**
     * 组件/网络唯一标识
     */
    @TableField(COMPONENT_ID)
    private int componentId;

    /**
     * 目标网段IP
     */
    @TableField(DEST_IP)
    private String destIp;

    /**
     * 子网掩码
     */
    @TableField(CIDR)
    private int cidr;

    /**
     * 下一跳IP
     */
    @TableField(NEXTHOP)
    private String nexthop;

    /**
     * 创建时间
     */
    @TableField(CREATE_TIME)
    private Date createTime;
}