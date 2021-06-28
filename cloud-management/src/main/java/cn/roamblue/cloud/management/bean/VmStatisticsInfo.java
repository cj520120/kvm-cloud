package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Vm统计信息
 *
 * @author chenjun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VmStatisticsInfo {
    /**
     * 创建时间
     */
    private Date time;
    /**
     * 磁盘读取速率
     */
    private float read;
    /**
     * 磁盘写入速率
     */
    private float write;
    /**
     * 网卡发送速率
     */
    private float send;
    /**
     * 网卡接收速率
     */
    private float receive;
    /**
     * CPU使用率
     */
    private float cpu;
}
