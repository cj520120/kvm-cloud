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
     * @param volumeId
     * @param vmId
     * @return
     */
    VolumeInfo detachVm(int volumeId, int vmId);

    VolumeInfo resume(int id);

    void destroyByVmId(int vmId);

    TemplateInfo createTemplateById(int id, int osCategoryId, String name);

    VolumeInfo resize(int id, long size);
}
