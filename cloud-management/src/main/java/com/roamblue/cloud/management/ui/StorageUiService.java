package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.StorageInfo;

import java.util.List;

public interface StorageUiService {
    ResultUtil<List<StorageInfo>> listStorage();

    ResultUtil<List<StorageInfo>> search(int clusterId);

    ResultUtil<StorageInfo> findStorageById(int id);


    ResultUtil<StorageInfo> createStorage(int clusterId, String name, String uri, String source);


    ResultUtil<Void> destroyStorageById(int id);
}
