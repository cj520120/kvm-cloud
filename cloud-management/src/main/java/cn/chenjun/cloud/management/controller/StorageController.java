package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.annotation.Login;
import cn.chenjun.cloud.management.model.StorageModel;
import cn.chenjun.cloud.management.servcie.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chenjun
 */
@Login
@RestController
public class StorageController {
    @Autowired
    private StorageService storageService;

    @GetMapping("/api/storage/all")
    public ResultUtil<List<StorageModel>> listStorage() {
        return storageService.listStorage();
    }

    @GetMapping("/api/storage/info")
    public ResultUtil<StorageModel> getStorageInfo(@RequestParam("storageId") int storageId) {
        return storageService.getStorageInfo(storageId);
    }

    @PutMapping("/api/storage/create")
    public ResultUtil<StorageModel> createStorage(@RequestParam("name") String name,
                                                  @RequestParam("type") String type,
                                                  @RequestParam("param") String param) {
        return storageService.createStorage(name, type, param);
    }

    @PostMapping("/api/storage/register")
    public ResultUtil<StorageModel> registerStorage(int storageId) {
        return storageService.registerStorage(storageId);
    }

    @PostMapping("/api/storage/maintenance")
    public ResultUtil<StorageModel> maintenanceStorage(int storageId) {
        return storageService.maintenanceStorage(storageId);
    }

    @DeleteMapping("/api/storage/destroy")
    public ResultUtil<StorageModel> destroyStorage(int storageId) {
        return storageService.destroyStorage(storageId);

    }

}
