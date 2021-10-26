package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 计算方案
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculationSchemeInfo implements Serializable {
    /**
     * ID
     */
    private int id;
    /**
     * 名称
     */
    private String name;
    /**
     * cpu
     */
    private int cpu;
    /**
     * 内存
     */
    private long memory;
    /**
     * 评率
     */
    private int speed;
    /**
     * 套接字数量
     */
    private int socket;
    /**
     * 每个套接字核心数
     */
    private  int core;
    /**
     * 超线程数
     */
    private int threads;
    /**
     * 创建时间
     */
    private Date createTime;

}
