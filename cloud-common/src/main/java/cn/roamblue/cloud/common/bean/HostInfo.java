package cn.roamblue.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 主机信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostInfo implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * 主机名称
     */
    private String hostName;
    /**
     * Libvirt版本
     */
    private long version;
    /**
     * 连接地址
     */
    private String uri;
    /**
     * 内存
     */
    private long memory;
    /**
     * cpu
     */
    private int cpu;
    /**
     * 系统架构
     */
    private String arch;
    /**
     * 系统名称
     */
    private String name;
    /**
     * hypervisor类型
     */
    private String hypervisor;
    /**
     * emulator
     */
    private String emulator;
}
