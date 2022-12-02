package cn.roamblue.cloud.management.v2.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_host_info")
public class HostEntity {
    private int id;
    private int clusterId;
    private String ip;
    private String nic;
    private int port;
    /**
     * 主机名称
     */
    private String hostName;
    /**
     * 主机ID
     */
    private String hostId;
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
    /**
     * 状态
     */
    private int status;

}
