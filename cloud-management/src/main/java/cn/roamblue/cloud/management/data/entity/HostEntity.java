package cn.roamblue.cloud.management.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_host_info")
public class HostEntity {
    @TableId(type = IdType.AUTO)
    @TableField("host_id")
    private Integer hostId;
    @TableField("host_display_name")
    private String displayName;

    @TableField("client_id")
    private String clientId;

    @TableField("client_secret")
    private String clientSecret;

    @TableField("host_ip")
    private String hostIp;
    @TableField("host_name")
    private String hostName;
    @TableField("host_nic_name")
    private String nic;
    @TableField("host_uri")
    private String uri;
    @TableField("host_allocation_memory")
    private long allocationMemory;
    @TableField("host_allocation_cpu")
    private int allocationCpu;
    @TableField("host_total_memory")
    private long totalMemory;
    @TableField("host_total_cpu")
    private int totalCpu;
    @TableField("host_arch")
    private String arch;
    @TableField("host_hypervisor")
    private String hypervisor;
    @TableField("host_emulator")
    private String emulator;
    @TableField("host_status")
    private int status;
    @TableField("create_time")
    private Date createTime;

}
