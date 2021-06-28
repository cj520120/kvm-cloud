package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 集群信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClusterInfo implements Serializable {
    /**
     * 集群ID
     */
    private int id;
    /**
     * 集群名称
     */
    private String name;
    /**
     * cpu超配比例
     */
    private float overCpu;
    /**
     * 内存超配比例
     */
    private float overMemory;
    /**
     * 集群状态
     */
    private String status;
    /**
     * 创建时间
     */
    private Date createTime;
}
