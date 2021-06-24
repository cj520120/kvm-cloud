package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.VolumeInfo;

import java.util.List;

/**
 * @author chenjun
 */
public interface VolumeUiService {
    /**
     * 获取磁盘列表
     *
     * @return
     */
    ResultUtil<List<VolumeInfo>> listVolume();

    /**
     * 搜索
     *
     * @param clusterId
     * @param storageId
     * @param vmId
     * @return
     */
    ResultUtil<List<VolumeInfo>> search(int clusterId, int storageId, int vmId);

    /**
     * 根据ID获取磁盘信息
     *
     * @param id
     * @return
     */
    ResultUtil<VolumeInfo> findVolumeById(int id);

    /**
     * 创建磁盘
     *
     * @param clusterId
     * @param storageId
     * @param name
     * @param size
     * @return
     */
    ResultUtil<VolumeInfo> createVolume(int clusterId, int storageId, String name, long size);

    /**
     * 销毁磁盘
     *
     * @param id
     * @return
     */
    ResultUtil<VolumeInfo> destroyVolumeById(int id);

    /**
     * 恢复磁盘
     *
     * @param id
     * @return
     */
    ResultUtil<VolumeInfo> resume(int id);

    /**
     * 扩容磁盘
     *
     * @param id
     * @param size
     * @return
     */
    ResultUtil<VolumeInfo> resize(int id, long size);
}
