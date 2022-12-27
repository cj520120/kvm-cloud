package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.annotation.Login;
import cn.roamblue.cloud.management.model.StorageModel;
import cn.roamblue.cloud.management.servcie.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
