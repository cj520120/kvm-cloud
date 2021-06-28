package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostInfo implements Serializable {
    /**
     * id
     */
    private int id;
    /**
     * 集群ID
     */
    private int clusterId;
    /**
     * 主机名称
     */
    private String name;
    /**
     * 主机地址
     */
    private String ip;
    /**
     * agent地址
     */
    private String uri;
    /**
     * 状态
     */
    private String status;
    /**
     * 主机内存
     */
    private long memory;
    /**
     * 主机核心数
     */
    private int cpu;
    /**
     * 已用内存
     */
    private long allocationMemory;
    /**
     * 已用核心数
     */
    private int allocationCpu;
    /**
     * 创建时间
     */
    private Date createTime;
}
