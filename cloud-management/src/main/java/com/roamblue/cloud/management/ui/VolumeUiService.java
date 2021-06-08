package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.VolumeInfo;

import java.util.List;

public interface VolumeUiService {
    ResultUtil<List<VolumeInfo>> listVolume();

    ResultUtil<List<VolumeInfo>> search(int clusterId, int storageId, int vmId);

    ResultUtil<VolumeInfo> findVolumeById(int id);

    ResultUtil<VolumeInfo> createVolume(int clusterId, int storageId, String name, long size);

    ResultUtil<VolumeInfo> destroyVolumeById(int id);

    ResultUtil<VolumeInfo> resume(int id);

    ResultUtil<VolumeInfo> resize(int id, long size);
}
