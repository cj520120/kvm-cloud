package com.roamblue.cloud.management.service;

import com.roamblue.cloud.common.agent.*;
import com.roamblue.cloud.common.bean.ResultUtil;

import java.util.List;
import java.util.Map;


public interface AgentService {

    ResultUtil<List<StorageModel>> getHostStorage(String uri);

    ResultUtil<HostModel> getHostInfo(String uri);

    ResultUtil<List<VmInfoModel>> getInstance(String uri);

    ResultUtil<StorageModel> addHostStorage(String uri, String host, String source, String target);

    ResultUtil<VolumeModel> createVolume(String uri, String storage, String volume, String backingVolume, long size);

    ResultUtil<VolumeModel> resize(String uri, String storageTarget, String volumeTarget, long size);

    ResultUtil<Void> destroyVolume(String uri, String storage, String volume);


    ResultUtil<Void> destroyStorage(String uri, String storage);

    ResultUtil<Void> destroyVm(String uri, String vm);

    ResultUtil<Void> stopVm(String uri, String vm);

    ResultUtil<Void> rebootVm(String uri, String vm);

    ResultUtil<Void> writeFile(String uri, String vm, String path, String body);

    ResultUtil<Map<String, Object>> execute(String uri, String vm, String command);

    ResultUtil<Void> changeCdRoom(String uri, String vm, String path);

    ResultUtil<Void> attachDisk(String uri, String vm, VmModel.Disk disk, boolean attach);

    ResultUtil<VmInfoModel> startVm(String uriInfo, VmModel kvm);

    ResultUtil<VolumeModel> getVolumeInfo(String uri, String storageName, String volumeName);

    ResultUtil<VolumeModel> cloneVolume(String uri, String sourceStorage, String sourceVolume, String targetStorage, String targetStorage1, String path);


    ResultUtil<List<VmStaticsModel>> listVmStatics(String uri);
}
