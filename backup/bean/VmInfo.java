package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * VM信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VmInfo implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * 虚拟机ID
     */
    private int id;
    /**
     * 虚拟机集群
     */
    private int clusterId;
    /**
     * 主机ID
     */
    private int hostId;
    /**
     * 群组ID
     */
    private int groupId;
    /**
     * 计算方案
     */
    private int calculationSchemeId;
    /**
     * 光盘
     */
    private int iso;
    /**
     * 模版ID
     */
    private int templateId;
    /**
     * IP
     */
    private String ip;
    /**
     * VNC端口
     */
    private int vncPort;
    /**
     * VNC密码
     */
    private String vncPassword;
    /**
     * 虚拟机名称
     */
    private String name;
    /**
     * 虚拟机备注
     */
    private String description;
    /**
     * 虚拟机类型
     */
    private String type;
    /**
     * 虚拟机状态
     */
    private String status;
    /**
     * 创建时间
     */
    private Date createTime;
}
