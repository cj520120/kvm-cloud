package cn.chenjun.cloud.management.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_host_info")
public class HostEntity {
    public static final String HOST_ID = "host_id";
    public static final String HOST_DISPLAY_NAME = "host_display_name";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String HOST_IP = "host_ip";
    public static final String HOST_NAME = "host_name";
    public static final String HOST_OS_NAME = "host_os_name";
    public static final String HOST_OS_VERSION = "host_os_version";
    public static final String HOST_NIC_NAME = "host_nic_name";
    public static final String HOST_URI = "host_uri";
    public static final String HOST_ALLOCATION_MEMORY = "host_allocation_memory";
    public static final String HOST_ALLOCATION_CPU = "host_allocation_cpu";
    public static final String HOST_TOTAL_MEMORY = "host_total_memory";
    public static final String HOST_TOTAL_CPU = "host_total_cpu";
    public static final String HOST_CPU_ARCH = "host_cpu_arch";
    public static final String HOST_CPU_VENDOR = "host_cpu_vendor";
    public static final String HOST_HYPERVISOR = "host_hypervisor";
    public static final String HOST_EMULATOR = "host_emulator";
    public static final String HOST_CPU_CORES = "host_cpu_cores";
    public static final String HOST_CPU_THREADS = "host_cpu_threads";
    public static final String HOST_CPU_SOCKETS = "host_cpu_sockets";
    public static final String HOST_CPU_MODEL = "host_cpu_model";
    public static final String HOST_CPU_FREQUENCY = "host_cpu_frequency";
    public static final String HOST_STATUS = "host_status";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO, value = HOST_ID)
    private Integer hostId;
    @TableField(HOST_DISPLAY_NAME)
    private String displayName;
    @TableField(CLIENT_ID)
    private String clientId;
    @TableField(CLIENT_SECRET)
    private String clientSecret;
    @TableField(HOST_IP)
    private String hostIp;
    @TableField(HOST_NAME)
    private String hostName;
    @TableField(HOST_OS_NAME)
    private String osName;
    @TableField(HOST_OS_VERSION)
    private String osVersion;
    @TableField(HOST_NIC_NAME)
    private String nic;
    @TableField(HOST_URI)
    private String uri;
    @TableField(HOST_ALLOCATION_MEMORY)
    private Long allocationMemory;
    @TableField(HOST_ALLOCATION_CPU)
    private Integer allocationCpu;
    @TableField(HOST_TOTAL_MEMORY)
    private Long totalMemory;
    @TableField(HOST_TOTAL_CPU)
    private Integer totalCpu;
    @TableField(HOST_CPU_ARCH)
    private String arch;
    @TableField(HOST_CPU_VENDOR)
    private String vendor;
    @TableField(HOST_HYPERVISOR)
    private String hypervisor;
    @TableField(HOST_EMULATOR)
    private String emulator;
    @TableField(HOST_CPU_MODEL)
    private String model;
    @TableField(HOST_CPU_FREQUENCY)
    private Long frequency;
    @TableField(HOST_CPU_CORES)
    private Integer cores;
    @TableField(HOST_CPU_THREADS)
    private Integer threads;
    @TableField(HOST_CPU_SOCKETS)
    private Integer sockets;
    @TableField(HOST_STATUS)
    private Integer status;
    @TableField(CREATE_TIME)
    private Date createTime;

}