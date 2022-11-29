package cn.roamblue.cloud.common.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 磁盘
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolumeRequest {
    /**
     * 命令
     */
    private String command;
    /**
     * 磁盘名称
     */
    private String name;
    /**
     * 创建磁盘信息
     */
    private CreateVolume create;
    /**
     * 克隆磁盘信息
     */
    private CloneVolume clone;
    /**
     * 扩容磁盘信息
     */
    private ResizeVolume resize;
    /**
     * 销毁磁盘信息
     */
    private DestroyVolume destroy;
    /**
     * 下载信息
     */
    private DownloadVolume download;
    /**
     * 模版信息
     */
    private TemplateVolume template;
    /**
     * 快照信息
     */
    private SnapshotVolume snapshot;
    /**
     * 迁移信息
     */
    private MigrateVolume migrate;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateVolume {
        private String parentStorage;
        private String parentVolume;
        private String parentType;
        private String targetStorage;
        private String targetVolume;
        private String targetType;
        private int size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResizeVolume {
        private String sourceStorage;
        private String sourceVolume;
        private String sourceType;
        private int size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CloneVolume {
        private String sourceStorage;
        private String sourceVolume;
        private String targetName;
        private String targetStorage;
        private String targetVolume;
        private String targetType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateVolume {
        private String sourceStorage;
        private String sourceVolume;
        private String sourceType;
        private String targetStorage;
        private String targetVolume;
        private String targetType;

    } @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MigrateVolume {
        private String sourceStorage;
        private String sourceVolume;
        private String sourceType;
        private String targetStorage;
        private String targetVolume;
        private String targetType;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SnapshotVolume {
        private String sourceStorage;
        private String sourceVolume;
        private String sourceType;
        private String targetStorage;
        private String targetVolume;
        private String targetType;
        private boolean inner;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DestroyVolume {
        private String sourceStorage;
        private String sourceVolume;
        private String sourceType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DownloadVolume {
        private String sourceUri;
        private String sourceType;
        private String targetVolume;
    }
}
