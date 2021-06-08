package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.StorageInfo;

import java.util.List;

public interface StorageService {
    /**
     * @return
     */
    List<StorageInfo> listStorage();

    /**
     * @param clusterId
     * @return
     */
    List<StorageInfo> search(int clusterId);


    /**
     * 根据ID获取
     *
     * @param id
     * @return
     */
    StorageInfo findStorageById(int id);

    /**
     * 创建
     *
     * @return
     */
    StorageInfo createStorage(int clusterId, String name, String uri, String source);

    /**
     * 销毁
     *
     * @return
     */
    void destroyStorageById(int id);


}
