package com.roamblue.cloud.common.agent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author chenjun
 */
@Data
@ApiModel("VM")
public class VmModel implements Serializable {
    @ApiModelProperty("ID")
    private int id;
    @ApiModelProperty("cpu信息")
    private Cpu cpu;
    @ApiModelProperty("内存信息")
    private Memory memory;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("备注")
    private String description;
    @ApiModelProperty("光盘路径")
    private String cdRoom;
    @ApiModelProperty("根磁盘")
    private RootDisk root;
    @ApiModelProperty("vnc密码")
    private String password;
    @ApiModelProperty("挂载磁盘")
    private List<Disk> disks;
    @ApiModelProperty("网卡列表")
    private List<Network> netwroks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("更新设备信息")
    public static class UpdateDisk implements Serializable {
        @ApiModelProperty("是否挂载")
        private boolean attach;
        @ApiModelProperty("虚拟机名称")
        private String name;
        @ApiModelProperty("设备信息")
        private Disk disk;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("Cpu")
    public static class Cpu implements Serializable {
        @ApiModelProperty("CPU核数")
        private int cpu;
        @ApiModelProperty("CPU速率")
        private int speed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ApiModel("磁盘信息")
    public static class Disk implements Serializable {
        @ApiParam(value = "路径")
        private String path;
        @ApiParam(value = "挂载设备", defaultValue = "1")
        private int device;

    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("内存")
    public static class Memory implements Serializable {
        @ApiParam(value = "内存大小")
        private long memory;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("桥接网卡信息")
    public static class Network implements Serializable {
        @ApiParam(value = "mac地址", defaultValue = "00:0c:29:eb:31:24")
        private String mac;
        @ApiParam(value = "桥接网卡名称", defaultValue = "br0")
        private String source;
        @ApiParam(value = "device", defaultValue = "0")
        private Integer device;
        @ApiParam(value = "drive", defaultValue = "virtio")
        private String driver;


    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ApiModel("根磁盘信息")
    public static class RootDisk implements Serializable {
        @ApiParam(value = "路径")
        private String path;
        @ApiParam(value = "设备类型", defaultValue = "virtio")
        private String driver;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("更新设备信息")
    public static class UpdateCdRoom implements Serializable {
        @ApiModelProperty("虚拟机名称")
        private String name;
        @ApiModelProperty("是否挂载")
        private boolean attach;
        @ApiModelProperty("设备信息")
        private String path;
    }
}
