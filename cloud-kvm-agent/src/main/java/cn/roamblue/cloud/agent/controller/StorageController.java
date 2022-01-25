package cn.roamblue.cloud.agent.controller;

import cn.roamblue.cloud.agent.service.KvmStorageService;
import cn.roamblue.cloud.common.agent.StorageModel;
import cn.roamblue.cloud.common.bean.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * KVM存储池管理
 *
 * @author chenjun
 */
@RestController
public class StorageController {
    @Autowired
    private KvmStorageService storageService;

    /**
     * 获取存储池列表
     *
     * @return
     */
    @GetMapping("/storage/list")
    public ResultUtil<List<StorageModel>> listStorage() {
        return ResultUtil.<List<StorageModel>>builder().data(storageService.listStorage()).build();
    }

    /**
     * 获取存储池信息
     *
     * @param name 存储池名称
     * @return
     */
    public ResultUtil<StorageModel> getStorageInfo(@RequestParam("name") String name) {
        return ResultUtil.<StorageModel>builder().data(storageService.getStorageInfo(name)).build();
    }

    /**
     * 销毁存储池
     *
     * @param name 存储池名称
     * @return
     */
    @PostMapping("/storage/destroy")
    public ResultUtil<Void> destroyStorage(@RequestParam("name") String name) {
        storageService.destroyStorage(name);
        return ResultUtil.<Void>builder().build();
    }

    /**
     * 创建存储池
     *
     * @param name   存储池名称
     * @param uri    存储池地址
     * @param path   存储路径
     * @param target 挂载路径
     * @return
     */
    @PostMapping("/storage/create")
    public ResultUtil<StorageModel> createStorage(@RequestParam("type") String type, @RequestParam("name") String name, @RequestParam("uri") String uri, @RequestParam("path") String path, @RequestParam("target") String target) {
        return ResultUtil.<StorageModel>builder().data(storageService.createStorage(type,name, uri, path, target)).build();

    }

}
