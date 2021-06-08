package com.roamblue.cloud.management.controller;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.VolumeInfo;
import com.roamblue.cloud.management.ui.VolumeUiService;
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
@Api(tags = "磁盘管理")
@Slf4j
public class VolumeController {
    @Autowired
    private VolumeUiService volumeUiService;

    @GetMapping("/management/volume")
    @ApiOperation(value = "磁盘列表")
    @ApiImplicitParams({
    })
    public ResultUtil<List<VolumeInfo>> listVolume() {
        return volumeUiService.listVolume();
    }

    @GetMapping("/management/volume/search")
    @ApiOperation(value = "搜索磁盘列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "集群ID"),
            @ApiImplicitParam(name = "vmId", value = "VM ID"),
            @ApiImplicitParam(name = "storageId", value = "存储池ID"),
    })
    public ResultUtil<List<VolumeInfo>> search(@RequestParam("clusterId") int clusterId,
                                               @RequestParam("vmId") int vmId,
                                               @RequestParam("storageId") int storageId) {
        return volumeUiService.search(clusterId, storageId, vmId);
    }


    @PostMapping("/management/volume/info")
    @ApiOperation(value = "获取磁盘信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "磁盘ID")
    })
    public ResultUtil<VolumeInfo> findVolumeById(@RequestParam("id") int id) {
        return volumeUiService.findVolumeById(id);
    }

    @PostMapping("/management/volume/create")
    @ApiOperation(value = "创建磁盘")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "集群ID"),
            @ApiImplicitParam(name = "storageId", value = "存储池"),
            @ApiImplicitParam(name = "name", value = "磁盘名称"),
            @ApiImplicitParam(name = "size", value = "磁盘"),
    })
    public ResultUtil<VolumeInfo> createVolume(
            @RequestParam("clusterId") int clusterId,
            @RequestParam("storageId") int storageId,
            @RequestParam("name") String name,
            @RequestParam("size") long size) {
        return volumeUiService.createVolume(clusterId, storageId, name, size);
    }

    @PostMapping("/management/volume/destroy")
    @ApiOperation(value = "销毁磁盘")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "磁盘ID")
    })
    public ResultUtil<VolumeInfo> destroyVolumeById(@RequestParam("id") int id) {
        return volumeUiService.destroyVolumeById(id);
    }

    @PostMapping("/management/volume/resume")
    @ApiOperation(value = "恢复磁盘")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "磁盘ID")
    })
    public ResultUtil<VolumeInfo> resume(@RequestParam("id") int id) {
        return volumeUiService.resume(id);
    }

    @PostMapping("/management/volume/resize")
    @ApiOperation(value = "扩容磁盘")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "磁盘ID"),
            @ApiImplicitParam(name = "size", value = "磁盘大小"),
    })
    public ResultUtil<VolumeInfo> resize(@RequestParam("id") int id, @RequestParam("size") long size) {
        return volumeUiService.resize(id, size);
    }
}
