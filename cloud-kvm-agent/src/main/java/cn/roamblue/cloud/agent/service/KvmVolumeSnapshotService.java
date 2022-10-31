package cn.roamblue.cloud.agent.service;

import cn.roamblue.cloud.common.agent.VolumeSnapshotModel;

import java.util.List;

/**
 * @author chenjun
 */
public interface KvmVolumeSnapshotService {
    /**
     * 快照列表
     *
     * @param file 磁盘路径
     * @return
     */
    List<VolumeSnapshotModel> listSnapshot(String file);

    /**
     * 创建快照
     *
     * @param name 快照名称
     * @param file 磁盘路径
     * @return
     */
    VolumeSnapshotModel createSnapshot(String name, String file);

    /**
     * 恢复快照
     *
     * @param name 快照名称
     * @param file 磁盘路径
     */
    void revertSnapshot(String name, String file);

    /**
     * 删除快照
     *
     * @param name 快照名称
     * @param file 磁盘路径
     */
    void deleteSnapshot(String name, String file);
}
