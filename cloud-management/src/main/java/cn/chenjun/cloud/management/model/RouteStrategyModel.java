package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RouteStrategyModel {
    /**
     * 主键ID
     */
    private int id;

    /**
     * 组件/网络唯一标识
     */
    private int componentId;

    /**
     * 目标网段IP
     */
    private String destIp;

    /**
     * 子网掩码
     */
    private int cidr;

    /**
     * 下一跳IP
     */
    private String nexthop;

    /**
     * 创建时间
     */
    private Date createTime;
}
