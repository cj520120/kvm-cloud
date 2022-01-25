package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 磁盘信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolumeInfo implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * id
     */
    private int id;
    /**
     * 集群ID
     */
    private int clusterId;
    /**
     * 存储池ID
     */
    private int storageId;
    /**
     * 实例ID
     */
    private int vmId;
    /**
     * 挂载设备
     */
    private int device;
    /**
     * 路径
     */
    private String target;
    /**
     * 存储名称
     */
    private String name;
    /**
     * 磁盘状态
     */
    private String status;
    /**
     * 物理大小
     */
    private long capacity;
    /**
     * 申请容量
     */
    private long allocation;
    /**
     * 创建时间
     */
    private Date createTime;
}
