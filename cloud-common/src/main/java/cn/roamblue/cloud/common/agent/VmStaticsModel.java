package cn.roamblue.cloud.common.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 监控信息
 *
 * @author chenjun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VmStaticsModel implements Serializable {
    /**
     * 虚拟机名称
     */
    private String name;
    /**
     * 时间
     */
    private long time;
    /**
     * 磁盘读取速率
     */
    private long diskReadSpeed;
    /**
     * 磁盘写入速率
     */
    private long diskWriteSpeed;
    /**
     * 网络发送速率
     */
    private long networkSendSpeed;
    /**
     * 网络接收速率
     */
    private long networkReceiveSpeed;
    /**
     * cpu使用率
     */
    private int cpuUsage;
}
