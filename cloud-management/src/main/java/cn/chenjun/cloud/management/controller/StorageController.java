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
                                                  @RequestParam("supportCategory") int supportCategory,
                                                  @RequestParam("type") String type,
                                                  @RequestParam("param") String param) {
        return this.lockRun(() -> storageService.createStorage(supportCategory, description, type, param));
    }


    @PostMapping("/api/storage/support/category/update")
    public ResultUtil<StorageModel> updateStorageSupportCategory(@RequestParam("storageId") int storageId, @RequestParam("supportCategory") int supportCategory) {
        return this.lockRun(() -> storageService.updateStorageSupportCategory(storageId, supportCategory));
    }

    @PostMapping("/api/storage/register")
    public ResultUtil<StorageModel> registerStorage(@RequestParam("storageId") int storageId) {
        return this.lockRun(() -> storageService.registerStorage(storageId));
    }

    @PostMapping("/api/storage/migrate")
    public ResultUtil<Void> migrateStorage(@RequestParam("sourceStorageId") int sourceStorageId, @RequestParam("destStorageId") int destStorageId) {
        return this.lockRun(() -> storageService.migrateStorage(sourceStorageId, destStorageId));
    }

    @PostMapping("/api/storage/maintenance")
    public ResultUtil<StorageModel> maintenanceStorage(@RequestParam("storageId") int storageId) {
        return this.lockRun(() -> storageService.maintenanceStorage(storageId));
    }

    @DeleteMapping("/api/storage/destroy")
    public ResultUtil<StorageModel> destroyStorage(@RequestParam("storageId") int storageId) {
        return this.lockRun(() -> storageService.destroyStorage(storageId));

    }

    @DeleteMapping("/api/storage/clear")
    public ResultUtil<Void> clearUnLinkVolume(@RequestParam("storageId") int storageId) {
        return this.lockRun(() -> storageService.clearUnLinkVolume(storageId));

    }

}
