package com.roamblue.cloud.agent.service;

import com.roamblue.cloud.common.agent.StorageModel;

import java.util.List;

public interface KvmStorageService {
    List<StorageModel> listStorage();

    StorageModel getStorageInfo(String name);

    void destroyStorage(String name);

    StorageModel createStorage(String name, String nfs, String path, String target);
}
