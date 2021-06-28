package cn.roamblue.cloud.common.agent;

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
public class HostModel implements Serializable {
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
     * hypervisor类型
     */
    private String hypervisor;
}
