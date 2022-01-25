package cn.roamblue.cloud.common.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.libvirt.DomainInfo;

import java.io.Serializable;

/**
 * 虚拟机信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VmInfoModel implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * 虚拟机最大内存
     */
    public long maxMem;
    /**
     * 虚拟机内存Kib
     */
    public long memory;
    /**
     * 虚拟机cpu
     */
    public int cpu;
    /**
     * 虚拟机cpu时间
     */
    public long cpuTime;
    /**
     * 虚拟机名称
     */
    private String name;
    /**
     * 虚拟机UUID
     */
    private String uuid;
    /**
     * VNC端口号
     */
    private int vnc;
    /**
     * VNC密码
     */
    private String password;
    /**
     * 虚拟机状态
     */
    private DomainInfo.DomainState state;
}
