package com.roamblue.cloud.management.service;

import com.roamblue.cloud.common.agent.*;
import com.roamblue.cloud.common.bean.ResultUtil;

import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
public interface AgentService {
    /**
     * 查询主机存储池信息
     *
     * @param uri
     * @return
     */
    ResultUtil<List<StorageModel>> getHostStorage(String uri);

    /**
     * 获取主机信息
     *
     * @param uri
     * @return
     */
    ResultUtil<HostModel> getHostInfo(String uri);

    /**
     * 获取主机所有实例
     *
     * @param uri
     * @return
     */
    ResultUtil<List<VmInfoModel>> getInstance(String uri);

    /**
     * 添加主机存储池
     *
     * @param uri
     * @param host
     * @param source
     * @param target
     * @return
     */
    ResultUtil<StorageModel> addHostStorage(String uri, String host, String source, String target);

    /**
     * 创建磁盘卷
     *
     * @param uri
     * @param storage
     * @param volume
     * @param backingVolume
     * @param size
     * @return
     */
    ResultUtil<VolumeModel> createVolume(String uri, String storage, String volume, String backingVolume, long size);

    /**
     * 调整磁盘卷大小
     *
     * @param uri
     * @param storageTarget
     * @param volumeTarget
     * @param size
     * @return
     */
    ResultUtil<VolumeModel> resize(String uri, String storageTarget, String volumeTarget, long size);

    /**
     * 销毁存储卷
     *
     * @param uri
     * @param storage
     * @param volume
     * @return
     */
    ResultUtil<Void> destroyVolume(String uri, String storage, String volume);

    /**
     * 销毁存储池
     *
     * @param uri
     * @param storage
     * @return
     */
    ResultUtil<Void> destroyStorage(String uri, String storage);

    /**
     * 销毁实例
     *
     * @param uri
     * @param vm
     * @return
     */
    ResultUtil<Void> destroyVm(String uri, String vm);

    /**
     * 停止实例
     *
     * @param uri
     * @param vm
     * @return
     */
    ResultUtil<Void> stopVm(String uri, String vm);

    /**
     * 重启实例
     *
     * @param uri
     * @param vm
     * @return
     */
    ResultUtil<Void> rebootVm(String uri, String vm);

    /**
     * 通过qma写入文件
     *
     * @param uri
     * @param vm
     * @param path
     * @param body
     * @return
     */
    ResultUtil<Void> writeFile(String uri, String vm, String path, String body);

    /**
     * qma执行命令
     *
     * @param uri
     * @param vm
     * @param command
     * @return
     */
    ResultUtil<Map<String, Object>> execute(String uri, String vm, String command);

    /**
     * 更行光盘文件
     *
     * @param uri
     * @param vm
     * @param path
     * @return
     */
    ResultUtil<Void> changeCdRoom(String uri, String vm, String path);

    /**
     * 附加磁盘
     *
     * @param uri
     * @param vm
     * @param disk
     * @param attach
     * @return
     */
    ResultUtil<Void> attachDisk(String uri, String vm, VmModel.Disk disk, boolean attach);

    /**
     * 启动实例
     *
     * @param uriInfo
     * @param kvm
     * @return
     */
    ResultUtil<VmInfoModel> startVm(String uriInfo, VmModel kvm);

    /**
     * 获取磁盘卷信息
     *
     * @param uri
     * @param storageName
     * @param volumeName
     * @return
     */
    ResultUtil<VolumeModel> getVolumeInfo(String uri, String storageName, String volumeName);

    /**
     * 磁盘卷克隆
     *
     * @param uri
     * @param sourceStorage
     * @param sourceVolume
     * @param targetStorage
     * @param targetStorage1
     * @param path
     * @return
     */
    ResultUtil<VolumeModel> cloneVolume(String uri, String sourceStorage, String sourceVolume, String targetStorage, String targetStorage1, String path);

    /**
     * 获取实例监控信息
     *
     * @param uri
     * @return
     */
    ResultUtil<List<VmStaticsModel>> listVmStatics(String uri);
}
