package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 网络信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkInfo implements Serializable {
    /**
     * id
     */
    private int id;
    /**
     * 网络名称
     */
    private String name;
    /**
     * 集群ID
     */
    private int clusterId;
    /**
     * 管理分配开始IP
     */
    private String managerStartIp;
    /**
     * 管理分配结束IP
     */
    private String managerEndIp;
    /**
     * 虚拟机分配开始IP
     */
    private String guestStartIp;
    /**
     * 虚拟机分配结束IP
     */
    private String guestEndIp;
    /**
     * 子网信息
     */
    private String subnet;
    /**
     * 网关
     */
    private String gateway;
    /**
     * dns信息
     */
    private String dns;
    /**
     * 网卡名称
     */
    private String card;
    /**
     * 网络类型
     */
    private String type;
    /**
     * 网络状态
     */
    private String status;
    /**
     * 创建时间
     */
    private Date createTime;

}
