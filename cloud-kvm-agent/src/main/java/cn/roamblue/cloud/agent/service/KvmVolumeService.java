package cn.roamblue.cloud.agent.service;

import cn.roamblue.cloud.common.agent.VolumeModel;

import java.util.List;

/**
 * @author chenjun
 */
public interface KvmVolumeService {
    /**
     * 根据存储池获取磁盘列表
     *
     * @param storageName
     * @return
     */
    List<VolumeModel> listVolume(String storageName);

    /**
     * 获取磁盘信息
     *
     * @param storageName
     * @param volumeName
     * @return
     */
    VolumeModel getVolume(String storageName, String volumeName);

    /**
     * 磁盘扩容
     *
     * @param storageName
     * @param volumeName
     * @param size
     * @return
     */
    VolumeModel reSize(String storageName, String volumeName, long size);

    /**
     * 销毁磁盘
     *
     * @param storage
     * @param volume
     */
    void destroyVolume(String storage, String volume);

    /**
     * 创建磁盘
     *
     * @param storage
     * @param volume
     * @param path
     * @param capacityGb
     * @param backingStore
     * @return
     */
    VolumeModel createVolume(String storage, String volume, String path, long capacityGb, String backingStore);

    /**
     * 克隆磁盘
     *
     * @param sourceStorage
     * @param sourceVolume
     * @param targetStorage
     * @param targetVolume
     * @param path
     * @return
     */
    VolumeModel cloneVolume(String sourceStorage, String sourceVolume, String targetStorage, String targetVolume, String path);


}
