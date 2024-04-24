package cn.chenjun.cloud.management.model;

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
    private String vendor;
    private String uefiType;
    private String uefiPath;
    private String hypervisor;
    private String emulator;
    private String osName;
    private String osVersion;
    private String machine;
    private int cores;
    private int threads;
    private int sockets;
    private int status;
    private String clientId;
    private String clientSecret;
    private Date createTime;

}
