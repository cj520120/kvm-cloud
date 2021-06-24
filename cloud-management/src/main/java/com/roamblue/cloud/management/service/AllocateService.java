package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.data.entity.HostEntity;
import com.roamblue.cloud.management.data.entity.StorageEntity;

public interface AllocateService {
    /**
     * 申请存储池
     *
     * @param clusterId
     * @param storageId
     * @param size
     * @return
     */
    StorageEntity allocateStorage(int clusterId, int storageId, long size);

    /**
     * 申请主机
     *
     * @param clusterId
     * @param hostId
     * @param cpu
     * @param memory
     * @return
     */
    HostEntity allocateHost(int clusterId, int hostId, int cpu, long memory);
}
