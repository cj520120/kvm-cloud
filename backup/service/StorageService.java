package cn.roamblue.cloud.management.service;

import cn.roamblue.cloud.management.bean.StorageInfo;

import java.util.List;

/**
 * @author chenjun
 */
public interface StorageService {
    /**
     * 存储池列表
     *
     * @return
     */
    List<StorageInfo> listStorage();

    /**
     * 搜索存储池
     *
     * @param clusterId
     * @return
     */
    List<StorageInfo> search(int clusterId);


    /**
     * 根据ID获取存储池
     *
     * @param id
     * @return
     */
    StorageInfo findStorageById(int id);

    /**
     * 创建存储池
     *
     * @param clusterId
     * @param name
     * @param uri
     * @param source
     * @return
     */
    StorageInfo createStorage(int clusterId, String name, String uri, String source);

    /**
     * 销毁存储池
     *
     * @param id
     * @return
     */
    void destroyStorageById(int id);


}
