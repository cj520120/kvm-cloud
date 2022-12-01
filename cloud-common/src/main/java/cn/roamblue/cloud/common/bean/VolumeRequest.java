package cn.roamblue.cloud.common.bean;

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
    private VolumeCreateRequest create;
    /**
     * 克隆磁盘信息
     */
    private VolumeCloneRequest clone;
    /**
     * 扩容磁盘信息
     */
    private VolumeResizeRequest resize;
    /**
     * 销毁磁盘信息
     */
    private VolumeDestroyRequest destroy;
    /**
     * 下载信息
     */
    private VolumeDownloadRequest download;
    /**
     * 模版信息
     */
    private VolumeCreateTemplateRequest template;
    /**
     * 快照信息
     */
    private VolumeCreateSnapshotRequest snapshot;
    /**
     * 迁移信息
     */
    private VolumeMigrateRequest migrate;

}
