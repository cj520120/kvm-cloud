package com.roamblue.cloud.agent.controller;

import com.roamblue.cloud.agent.service.KvmStorageService;
import com.roamblue.cloud.common.agent.StorageModel;
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

/**
 * @author chenjun
 */
@RestController
@Api(tags = "KVM存储池管理")
@Slf4j
public class StorageController {
    @Autowired
    private KvmStorageService storageService;

    @GetMapping("/storage/list")

    @ApiImplicitParams({})
    @ApiOperation(value = "获取存储池列表")
    public ResultUtil<List<StorageModel>> listStorage() {
        return ResultUtil.<List<StorageModel>>builder().data(storageService.listStorage()).build();
    }

    @GetMapping("/storage/info")
    @ApiOperation(value = "获取存储池信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "name", value = "存储池名称"),

    })
    public ResultUtil<StorageModel> getStorageInfo(@RequestParam("name") String name) {
        return ResultUtil.<StorageModel>builder().data(storageService.getStorageInfo(name)).build();
    }

    @PostMapping("/storage/destroy")
    @ApiOperation(value = "销毁存储池")
    @ApiImplicitParams({@ApiImplicitParam(name = "name", value = "存储池名称"),

    })
    public ResultUtil<Void> destroyStorage(@RequestParam("name") String name) {
        storageService.destroyStorage(name);
        return ResultUtil.<Void>builder().build();
    }

    @PostMapping("/storage/create")
    @ApiOperation(value = "创建存储池")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "存储池名称"),
            @ApiImplicitParam(name = "nfs", value = "nfs服务器地址"),
            @ApiImplicitParam(name = "path", value = "nfs路径"),
            @ApiImplicitParam(name = "target", value = "目标路径"),


    })
    public ResultUtil<StorageModel> createStorage(@RequestParam("name") String name, @RequestParam("nfs") String nfs, @RequestParam("path") String path, @RequestParam("target") String target) {
        return ResultUtil.<StorageModel>builder().data(storageService.createStorage(name, nfs, path, target)).build();

    }

}
