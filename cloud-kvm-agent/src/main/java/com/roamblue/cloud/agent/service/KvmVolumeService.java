package com.roamblue.cloud.agent.service;

import com.roamblue.cloud.common.agent.VolumeModel;

import java.util.List;

public interface KvmVolumeService {
    List<VolumeModel> listVolume(String storageName);

    VolumeModel getVolume(String storageName, String volumeName);

    VolumeModel reSize(String storageName, String volumeName, long size);

    void destroyVolume(String storage, String volume);

    VolumeModel createVolume(String storage, String volume, String path, long capacityGb, String backingStore);

    VolumeModel cloneVolume(String sourceStorage, String sourceVolume, String targetStorage, String targetVolume, String path);
}
