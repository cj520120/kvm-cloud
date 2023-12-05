package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.annotation.LoginRequire;
import cn.chenjun.cloud.management.model.StorageModel;
import cn.chenjun.cloud.management.servcie.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chenjun
 */
@LoginRequire
@RestController
public class StorageController extends BaseController {
    @Autowired
    private StorageService storageService;

    @GetMapping("/api/storage/all")
    public ResultUtil<List<StorageModel>> listStorage() {
        return this.lockRun(() -> storageService.listStorage());
    }

    @GetMapping("/api/storage/info")
    public ResultUtil<StorageModel> getStorageInfo(@RequestParam("storageId") int storageId) {
        return this.lockRun(() -> storageService.getStorageInfo(storageId));
    }

    @PutMapping("/api/storage/create")
    public ResultUtil<StorageModel> createStorage(@RequestParam("description") String description,
                                                  @RequestParam("type") String type,
                                                  @RequestParam("param") String param) {
        return this.lockRun(() -> storageService.createStorage(description, type, param));
    }

    @PostMapping("/api/storage/register")
    public ResultUtil<StorageModel> registerStorage(int storageId) {
        return this.lockRun(() -> storageService.registerStorage(storageId));
    }

    @PostMapping("/api/storage/maintenance")
    public ResultUtil<StorageModel> maintenanceStorage(int storageId) {
        return this.lockRun(() -> storageService.maintenanceStorage(storageId));
    }

    @DeleteMapping("/api/storage/destroy")
    public ResultUtil<StorageModel> destroyStorage(int storageId) {
        return this.lockRun(() -> storageService.destroyStorage(storageId));

    }

}
