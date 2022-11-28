package cn.roamblue.cloud.common.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OsRequest {
    /**
     * 命令
     */
    private String command;
    /**
     * 虚拟机名称
     */
    private String name;

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
        /**
         * 虚拟机名称
         */
        private String name;

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

    }
}
