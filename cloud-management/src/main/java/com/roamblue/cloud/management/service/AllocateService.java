package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.data.entity.HostEntity;
import com.roamblue.cloud.management.data.entity.StorageEntity;

public interface AllocateService {
    StorageEntity allocateStorage(int clusterId, int storageId, long size);

    HostEntity allocateHost(int clusterId, int hostId, int cpu, long memory);
}
