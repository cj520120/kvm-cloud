package cn.roamblue.cloud.management.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HostModel {
    private int hostId;
    private String displayName;
    private String hostIp;
    private String hostName;
    private String nic;
    private String uri;
    private long allocationMemory;
    private int allocationCpu;
    private long totalMemory;
    private int totalCpu;
    private String arch;
    private String hypervisor;
    private String emulator;
    private int cores;
    private int threads;
    private int sockets;
    private int status;
    private Date createTime;

}
