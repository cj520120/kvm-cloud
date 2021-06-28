package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 存储池信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageInfo implements Serializable {
    /**
     * id
     */
    private int id;
    /**
     * 存储池名称
     */
    private String name;
    /**
     * 集群ID
     */
    private int clusterId;
    /**
     * 存储池地址
     */
    private String host;
    /**
     * 存储池来源地址
     */
    private String source;
    /**
     * 存储池挂载地址
     */
    private String target;
    /**
     * 存储池状态
     */
    private String status;
    /**
     * 容量
     */
    private long capacity;
    /**
     * 可用
     */
    private long available;
    /**
     * 已用
     */
    private long allocation;
    /**
     * 创建时间
     */
    private Date createTime;
}
