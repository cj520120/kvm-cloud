package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 网卡信息
 *
 * @author chenjun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VmNetworkInfo implements Serializable {
    /**
     * 网卡ID
     */
    private Integer id;
    /**
     * 所属网络
     */
    private Integer networkId;
    /**
     * 所属集群
     */
    private Integer clusterId;
    /**
     * 所属实例
     */
    private Integer vmId;
    /**
     * 设备
     */
    private Integer device;
    /**
     * mac
     */
    private String mac;
    /**
     * ip
     */
    private String ip;
    /**
     * 类型
     */
    private String type;
    /**
     * 状态
     */
    private String status;
    /**
     * 时间
     */
    private Date createTime;
}
