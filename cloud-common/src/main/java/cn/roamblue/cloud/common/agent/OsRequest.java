package cn.roamblue.cloud.common.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OsRequest {
    /**
     * 命令
     */
    private String command;

    private Start start;
    private Shutdown shutdown;
    private Destroy destroy;
    private Reboot reboot;
    private Qma qma;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Start{
        private String name;
        private String description;
        private Memory memory;
        private Cpu cpu;
        private List<CdRoom> cdRooms;
        private List<Disk> disks;
        private List<Nic> networkInterfaces;

    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Cpu{
        private int number;
        private int mode;
        private int socket;
        private int core;
        private int thread;
        private long speed;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Memory{
        private int memory;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Shutdown{
        /**
         * 虚拟机名称
         */
        private String name;

    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Destroy{
        /**
         * 虚拟机名称
         */
        private String name;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Reboot{
        /**
         * 虚拟机名称
         */
        private String name;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Qma{
        /**
         * 虚拟机名称
         */
        private String name;
        private int timeout;
        private List<QmaBody> commands;

        public static class QmaType{
            public static final int WRITE_FILE=0;
            public static final int EXECUTE=1;
        }
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class QmaBody{
            private int command;
            private String data;
        }
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class WriteFile{
            private String fileName;
            private String fileBody;
        }
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Execute{
            private String command;
            private String[] args;
        }

    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CdRoom{
        /**
         * 虚拟机名称
         */
        private String name;
        private int deviceId;
        private String path;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Disk{
        /**
         * 虚拟机名称
         */
        private String name;
        private String bus;
        private int deviceId;
        private String volume;
        private String volumeType;

        public static class DiskBus{
            public static final String VIRTIO="virtio";
            public static final String IDE="ide";
            public static final String SCSI="scsi";

        }
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Nic{
        /**
         * 虚拟机名称
         */
        private String name;
        private String driveType;
        private int deviceId;
        private String mac;
        private String bridgeName;
    }
}
