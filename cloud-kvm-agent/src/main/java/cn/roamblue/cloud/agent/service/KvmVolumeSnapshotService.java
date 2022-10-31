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
     * @param storage 存储池
     * @param volume 磁盘
     * @return
     */
    List<VolumeSnapshotModel> listSnapshot(String  storage,String volume);

    /**
     * 创建快照
     *
     * @param name 快照名称
     * @param storage 存储池
     * @param volume 磁盘
     * @return
     */
    VolumeSnapshotModel createSnapshot(String name, String  storage,String volume);

    /**
     * 恢复快照
     *
     * @param name 快照名称
     * @param storage 存储池
     * @param volume 磁盘
     */
    void revertSnapshot(String name, String  storage,String volume);

    /**
     * 删除快照
     *
     * @param name 快照名称
     * @param storage 存储池
     * @param volume 磁盘
     */
    void deleteSnapshot(String name, String  storage,String volume);
}
