package cn.roamblue.cloud.agent.controller;

import cn.roamblue.cloud.agent.service.KvmVolumeService;
import cn.roamblue.cloud.agent.service.KvmVolumeSnapshotService;
import cn.roamblue.cloud.common.agent.VolumeModel;
import cn.roamblue.cloud.common.agent.VolumeSnapshotModel;
import cn.roamblue.cloud.common.bean.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * KVM磁盘管理
 *
 * @author chenjun
 */
@RestController
public class VolumeController {

    @Autowired
    private KvmVolumeService volumeService;


    @Autowired
    private KvmVolumeSnapshotService volumeSnapshotService;

    /**
     * 获取磁盘列表
     *
     * @param storageName 存储池名称
     * @return
     */
    @GetMapping("/volume/list")
    public ResultUtil<List<VolumeModel>> listVolume(@RequestParam("storageName") String storageName) {
        return ResultUtil.<List<VolumeModel>>builder().data(volumeService.listVolume(storageName)).build();
    }

    /**
     * 获取磁盘信息
     *
     * @param storageName 存储池名称
     * @param volumeName  磁盘名称
     * @return
     */
    @GetMapping("/volume/info")
    public ResultUtil<VolumeModel> getVolume(
            @RequestParam("storageName") String storageName,
            @RequestParam("volumeName") String volumeName) {
        return ResultUtil.<VolumeModel>builder().data(volumeService.getVolume(storageName, volumeName)).build();
    }

    /**
     * 销毁磁盘
     *
     * @param storageName 存储池名称
     * @param volume      磁盘名称
     * @return
     */
    @PostMapping("/volume/destroy")
    public ResultUtil<Void> destroyVolume(
            @RequestParam("storageName") String storageName,
            @RequestParam("volumeName") String volume) {
        volumeService.destroyVolume(storageName, volume);
        return ResultUtil.<Void>builder().build();
    }

    /**
     * 扩容磁盘
     *
     * @param storageName 存储池名称
     * @param volume      磁盘名称
     * @param size        增加磁盘大小
     * @return
     */
    @PostMapping("/volume/resize")
    public ResultUtil<VolumeModel> resize(
            @RequestParam("storageName") String storageName,
            @RequestParam("volumeName") String volume,
            @RequestParam("size") long size) {
        return ResultUtil.<VolumeModel>builder().data(volumeService.reSize(storageName, volume, size)).build();
    }

    /**
     * 创建磁盘
     *
     * @param storageName   存储池名称
     * @param volumeName    磁盘名称
     * @param path          磁盘路径
     * @param capacity      磁盘大小GB,具有父磁盘时无效
     * @param backingVolume 父磁盘路径
     * @return
     */
    @PostMapping("/volume/create")
    public ResultUtil<VolumeModel> createVolume(
            @RequestParam("storageName") String storageName,
            @RequestParam("volumeName") String volumeName,
            @RequestParam("path") String path,
            @RequestParam("capacity") long capacity,
            @RequestParam("backingVolume") String backingVolume) {
        return ResultUtil.<VolumeModel>builder().data(volumeService.createVolume(storageName, volumeName, path, capacity, backingVolume)).build();
    }

    /**
     * 克隆磁盘
     *
     * @param sourceStorage 原存储池名称
     * @param sourceVolume  原磁盘名称
     * @param targetStorage 目标存储池名称
     * @param targetVolume  目标磁盘名称
     * @param targetPath    磁盘路径
     * @return
     */
    @PostMapping("/volume/clone")
    public ResultUtil<VolumeModel> cloneVolume(
            @RequestParam("sourceStorage") String sourceStorage,
            @RequestParam("sourceVolume") String sourceVolume,
            @RequestParam("targetStorage") String targetStorage,
            @RequestParam("targetVolume") String targetVolume,
            @RequestParam("targetPath") String targetPath) {
        return ResultUtil.<VolumeModel>builder().data(volumeService.cloneVolume(sourceStorage, sourceVolume, targetStorage, targetVolume, targetPath)).build();

    }


    /**
     * 快照列表
     *
     * @param storage 存储池
     * @param volume 磁盘
     * @return
     */
    @GetMapping("/volume/snapshot/list")
    public ResultUtil<List<VolumeSnapshotModel>> listSnapshot(
            @RequestParam("storage") String storage,
            @RequestParam("volume") String volume) {
        return ResultUtil.success(volumeSnapshotService.listSnapshot( storage,volume));
    }

    /**
     * 创建快照
     *
     * @param name 快照名称
     * @param storage 存储池
     * @param volume 磁盘
     * @return
     */
    @PostMapping("/volume/snapshot/create")
    public ResultUtil<VolumeSnapshotModel> createSnapshot(
            @RequestParam("storage") String storage,
            @RequestParam("volume") String volume,
            @RequestParam("name") String name) {
        return ResultUtil.success(volumeSnapshotService.createSnapshot(name,  storage,volume));
    }

    /**
     * 恢复快照
     *
     * @param name 快照名称
     * @param storage 存储池
     * @param volume 磁盘
     */
    @PostMapping("/volume/snapshot/revert")
    public ResultUtil<Void> revertSnapshot(
            @RequestParam("storage") String storage,
            @RequestParam("volume") String volume,
            @RequestParam("name") String name) {
        volumeSnapshotService.revertSnapshot(name,  storage,volume);
        return ResultUtil.<Void>builder().build();
    }

    /**
     * 删除快照
     *
     * @param name 快照名称
     * @param storage 存储池
     * @param volume 磁盘
     */
    @PostMapping("/volume/snapshot/delete")
    public ResultUtil<Void> deleteSnapshot(
            @RequestParam("storage") String storage,
            @RequestParam("volume") String volume,
            @RequestParam("name") String name) {
        volumeSnapshotService.deleteSnapshot(name, storage,volume);
        return ResultUtil.<Void>builder().build();
    }
}
