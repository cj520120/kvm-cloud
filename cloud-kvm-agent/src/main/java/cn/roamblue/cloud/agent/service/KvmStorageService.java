package cn.roamblue.cloud.agent.service;

import cn.roamblue.cloud.common.agent.StorageModel;

import java.util.List;

/**
 * @author chenjun
 */
public interface KvmStorageService {
    /**
     * 获取存储池
     *
     * @return
     */
    List<StorageModel> listStorage();

    /**
     * 根据ID获取存储池
     *
     * @param name
     * @return
     */
    StorageModel getStorageInfo(String name);

    /**
     * 销毁存储池
     *
     * @param name
     */
    void destroyStorage(String name);

    /**
     * 创建存储池
     *
     * @param type
     * @param name
     * @param uri
     * @param path
     * @param target
     * @return
     */
    StorageModel createStorage(String type,String name, String uri, String path, String target);
}
