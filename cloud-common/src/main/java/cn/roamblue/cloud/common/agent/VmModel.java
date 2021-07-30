package cn.roamblue.cloud.common.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * VM
 *
 * @author chenjun
 */
@Data
public class VmModel implements Serializable {
    /**
     * ID
     */
    private int id;
    /**
     * cpu信息
     */
    private Cpu cpu;
    /**
     * 内存信息
     */
    private Memory memory;
    /**
     * 名称
     */
    private String name;
    /**
     * 备注
     */
    private String description;
    /**
     * 光盘路径
     */
    private String cdRoom;
    /**
     * 根磁盘
     */
    private RootDisk root;
    /**
     * vnc密码
     */
    private String password;
    /**
     * 挂载磁盘
     */
    private List<Disk> disks;
    /**
     * 网卡列表
     */
    private List<Network> netwroks;

    /**
     * 更新设备信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDisk implements Serializable {
        /**
         * 是否挂载
         */
        private boolean attach;
        /**
         * 虚拟机名称
         */
        private String name;
        /**
         * 设备信息
         */
        private Disk disk;
    }

    /**
     * 更新设备信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateNetwork implements Serializable {
        /**
         * 是否挂载
         */
        private boolean attach;
        /**
         * 虚拟机名称
         */
        private String name;
        /**
         * 网卡信息
         */
        private Network network;
    }

    /**
     * Cpu
     */
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Cpu implements Serializable {
        /**
         * CPU核数
         */
        private int cpu;
        /**
         * CPU速率
         */
        private int speed;
    }

    /**
     * 磁盘信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Disk implements Serializable {
        /**
         * 路径
         */
        private String path;
        /**
         * 挂载设备
         */
        private int device;

    }

    /**
     * 内存
     */
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Memory implements Serializable {
        /**
         * 内存大小 Kib
         */
        private long memory;
    }

    /**
     * 桥接网卡信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Network implements Serializable {
        /**
         * mac地址
         */
        private String mac;
        /**
         * 桥接网卡名称
         */
        private String source;
        /**
         * device
         */
        private Integer device;
        /**
         * drive
         */
        @Builder.Default
        private String driver = "virtio";


    }

    /**
     * 根磁盘信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RootDisk implements Serializable {
        /**
         * 路径
         */
        private String path;
        @Builder.Default
        private String driver = "virtio";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCdRoom implements Serializable {
        /**
         * 虚拟机名称
         */
        private String name;
        /**
         * 是否挂载
         */
        private boolean attach;
        /**
         * 设备信息
         */
        private String path;
    }
}
