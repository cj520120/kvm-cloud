package cn.chenjun.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 主机信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostInfo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 主机名称
     */
    private String hostName;
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
     * cpu信息
     */
    private Cpu cpu;
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
     * 系统版本
     */
    private String osVersion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Topology {
        private int id;
        private int number;
        private long memory;
        private List<Cell> cells;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Cell {
            private int id;
            private int socketId;
            private int coreId;
        }
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Cpu{
        private int number;
        /**
         * 系统架构
         */
        private String arch;
        /**
         * 供应商
         */
        private String vendor;
        /**
         * 供应商 cpu型号
         */
        private String model;
        /**
         * 主频
         */
        private long frequency;
        /**
         * sockets
         */
        private Integer sockets;
        /**
         * cores
         */
        private Integer cores;
        /**
         * cores
         */
        private Integer threads;
        /**
         * cpu拓扑结构
         */
        private List<Topology> topology;
    }
}
