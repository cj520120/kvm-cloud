package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.StorageInfo;

import java.util.List;

/**
 * @author chenjun
 */
public interface StorageUiService {
    /**
     * 获取存储池列表
     *
     * @return
     */
    ResultUtil<List<StorageInfo>> listStorage();

    /**
     * 搜索存储池
     *
     * @param clusterId
     * @return
     */
    ResultUtil<List<StorageInfo>> search(int clusterId);

    /**
     * 根据ID查询存储池
     *
     * @param id
     * @return
     */
    ResultUtil<StorageInfo> findStorageById(int id);

    /**
     * 创建存储池
     *
     * @param clusterId
     * @param name
     * @param uri
     * @param source
     * @return
     */
    ResultUtil<StorageInfo> createStorage(int clusterId, String name, String uri, String source);

    /**
     * 销毁存储池
     *
     * @param id
     * @return
     */
    ResultUtil<Void> destroyStorageById(int id);
}
