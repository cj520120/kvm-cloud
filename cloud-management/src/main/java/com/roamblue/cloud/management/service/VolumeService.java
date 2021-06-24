package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.TemplateInfo;
import com.roamblue.cloud.management.bean.VolumeInfo;

import java.util.List;

public interface VolumeService {
    /**
     * 获取磁盘列表
     *
     * @return
     */
    List<VolumeInfo> listVolume();

    /**
     * 搜索磁盘列表
     *
     * @param clusterId
     * @param storageId
     * @param vmId
     * @return
     */
    List<VolumeInfo> search(int clusterId, int storageId, int vmId);


    /**
     * 获取磁盘列表
     *
     * @return
     */
    List<VolumeInfo> listVolumeByVmId(int vmId);


    /**
     * 根据ID获取磁盘
     *
     * @return
     */
    VolumeInfo findVolumeById(int id);

    /**
     * 创建磁盘
     *
     * @return
     */
    VolumeInfo createVolume(int clusterId, String parentVolumePath, int storageId, String name, long size);

    /**
     * 销毁磁盘
     *
     * @param id
     * @return
     */
    VolumeInfo destroyVolumeById(int id);

    /**
     * 挂载磁盘
     *
     * @param volumeId
     * @param vmId
     * @return
     */
    VolumeInfo attachVm(int volumeId, int vmId);

    /**
     * 卸载磁盘
     *
     * @param volumeId
     * @param vmId
     * @return
     */
    VolumeInfo detachVm(int volumeId, int vmId);

    /**
     * 恢复磁盘
     *
     * @param id
     * @return
     */
    VolumeInfo resume(int id);

    /**
     * 销毁磁盘
     *
     * @param vmId
     */
    void destroyByVmId(int vmId);

    /**
     * 创建磁盘模版
     *
     * @param id
     * @param osCategoryId
     * @param name
     * @return
     */
    TemplateInfo createTemplateById(int id, int osCategoryId, String name);

    /**
     * 磁盘扩容
     *
     * @param id
     * @param size
     * @return
     */
    VolumeInfo resize(int id, long size);
}
