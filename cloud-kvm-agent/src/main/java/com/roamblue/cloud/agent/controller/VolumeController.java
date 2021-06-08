package com.roamblue.cloud.agent.controller;

import com.roamblue.cloud.agent.service.KvmVolumeService;
import com.roamblue.cloud.common.agent.VolumeModel;
import com.roamblue.cloud.common.bean.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(tags = "KVM磁盘管理")
@Slf4j
public class VolumeController {

    @Autowired
    private KvmVolumeService volumeService;

    @GetMapping("/volume/list")
    @ApiOperation(value = "获取磁盘列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "storageName", value = "存储池名称")})
    public ResultUtil<List<VolumeModel>> listVolume(@RequestParam("storageName") String storageName) {
        return ResultUtil.<List<VolumeModel>>builder().data(volumeService.listVolume(storageName)).build();
    }

    @GetMapping("/volume/info")
    @ApiOperation(value = "获取磁盘信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storageName", value = "存储池名称"),
            @ApiImplicitParam(name = "volumeName", value = "磁盘名称"),
    })
    public ResultUtil<VolumeModel> getVolume(
            @RequestParam("storageName") String storageName,
            @RequestParam("volumeName") String volumeName) {
        return ResultUtil.<VolumeModel>builder().data(volumeService.getVolume(storageName, volumeName)).build();
    }

    @PostMapping("/volume/destroy")
    @ApiOperation(value = "销毁磁盘")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storageName", value = "存储池名称"),
            @ApiImplicitParam(name = "volumeName", value = "磁盘名称"),
    })
    public ResultUtil<Void> destroyVolume(
            @RequestParam("storageName") String storageName,
            @RequestParam("volumeName") String volume) {
        volumeService.destroyVolume(storageName, volume);
        return ResultUtil.<Void>builder().build();
    }

    @PostMapping("/volume/resize")
    @ApiOperation(value = "扩展磁盘")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storageName", value = "存储池名称"),
            @ApiImplicitParam(name = "volumeName", value = "磁盘名称"),
            @ApiImplicitParam(name = "size", value = "增加磁盘大小"),
    })
    public ResultUtil<VolumeModel> resize(
            @RequestParam("storageName") String storageName,
            @RequestParam("volumeName") String volume,
            @RequestParam("size") long size) {
        return ResultUtil.<VolumeModel>builder().data(volumeService.reSize(storageName, volume, size)).build();
    }

    @PostMapping("/volume/create")
    @ApiOperation(value = "创建磁盘")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storageName", value = "存储池名称"),
            @ApiImplicitParam(name = "volumeName", value = "磁盘名称"),
            @ApiImplicitParam(name = "path", value = "磁盘路径"),
            @ApiImplicitParam(name = "capacity", value = "磁盘大小GB,具有父磁盘时无效"),
            @ApiImplicitParam(name = "backingVolume", value = "父磁盘路径"),
    })
    public ResultUtil<VolumeModel> createVolume(
            @RequestParam("storageName") String storageName,
            @RequestParam("volumeName") String volumeName,
            @RequestParam("path") String path,
            @RequestParam("capacity") long capacity,
            @RequestParam("backingVolume") String backingVolume) {
        return ResultUtil.<VolumeModel>builder().data(volumeService.createVolume(storageName, volumeName, path, capacity, backingVolume)).build();
    }

    @PostMapping("/volume/clone")
    @ApiOperation(value = "克隆磁盘")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sourceStorage", value = "原存储池名称"),
            @ApiImplicitParam(name = "sourceVolume", value = "原磁盘名称"),
            @ApiImplicitParam(name = "targetStorage", value = "目标存储池名称"),
            @ApiImplicitParam(name = "targetVolume", value = "目标磁盘名称"),
            @ApiImplicitParam(name = "targetPath", value = "磁盘路径"),
    })
    public ResultUtil<VolumeModel> cloneVolume(
            @RequestParam("sourceStorage") String sourceStorage,
            @RequestParam("sourceVolume") String sourceVolume,
            @RequestParam("targetStorage") String targetStorage,
            @RequestParam("targetVolume") String targetVolume,
            @RequestParam("targetPath") String targetPath) {
        return ResultUtil.<VolumeModel>builder().data(volumeService.cloneVolume(sourceStorage, sourceVolume, targetStorage, targetVolume, targetPath)).build();

    }
}
