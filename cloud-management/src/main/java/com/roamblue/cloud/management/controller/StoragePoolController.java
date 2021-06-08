package com.roamblue.cloud.management.controller;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.annotation.Login;
import com.roamblue.cloud.management.bean.StorageInfo;
import com.roamblue.cloud.management.ui.StorageUiService;
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
@Api(tags = "存储池管理")
@Slf4j
public class StoragePoolController {
    @Autowired
    private StorageUiService storageUiService;

    @Login
    @GetMapping("/management/storage")
    @ApiOperation(value = "获取存储池列表")
    public ResultUtil<List<StorageInfo>> listStorages() {
        return storageUiService.listStorage();
    }

    @Login
    @GetMapping("/management/storage/search")
    @ApiOperation(value = "根据集群获取存储池列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "集群ID")
    })
    public ResultUtil<List<StorageInfo>> search(@RequestParam("clusterId") int clusterId) {
        return storageUiService.search(clusterId);
    }

    @Login
    @GetMapping("/management/storage/info")
    @ApiOperation(value = "获取存储池信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "存储池ID")
    })
    public ResultUtil<StorageInfo> findStorageById(@RequestParam("id") int id) {
        return storageUiService.findStorageById(id);
    }

    @Login
    @PostMapping("/management/storage/create")
    @ApiOperation(value = "创建存储池")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "集群ID"),
            @ApiImplicitParam(name = "name", value = "存储池名称"),
            @ApiImplicitParam(name = "uri", value = "存储池地址"),
            @ApiImplicitParam(name = "source", value = "存储池来源地址"),
    })
    public ResultUtil<StorageInfo> createStorage(
            @RequestParam("clusterId") int clusterId,
            @RequestParam("name") String name,
            @RequestParam("uri") String uri,
            @RequestParam("source") String source) {
        return storageUiService.createStorage(clusterId, name, uri, source);
    }

    @Login
    @PostMapping("/management/storage/destroy")
    @ApiOperation(value = "销毁存储池")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "存储池D")
    })
    public ResultUtil<Void> destroyStorageById(@RequestParam("id") int id) {
        return storageUiService.destroyStorageById(id);
    }


}
