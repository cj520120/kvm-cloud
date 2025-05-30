package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.SimpleStorageModel;
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
    public ResultUtil<List<SimpleStorageModel>> listStorage() {
        return this.lockRun(() -> storageService.listStorage());
    }

    @GetMapping("/api/storage/search")
    public ResultUtil<Page<SimpleStorageModel>> search(@RequestParam(value = "storageType", required = false) Integer storageType,
                                                 @RequestParam(value = "storageStatus",required = false) Integer storageStatus,
                                                 @RequestParam(value = "keyword",required = false) String keyword,
                                                 @RequestParam("no") int no,
                                                 @RequestParam("size") int size) {
        return this.lockRun(() -> storageService.search(storageType, storageStatus, keyword, no, size));
    }
    @GetMapping("/api/storage/info")
    public ResultUtil<StorageModel> getStorageInfo(@RequestParam("storageId") int storageId) {
        return this.lockRun(() -> storageService.getStorageInfo(storageId));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PutMapping("/api/storage/create")
    public ResultUtil<StorageModel> createStorage(@RequestParam("description") String description,
                                                  @RequestParam("supportCategory") int supportCategory,
                                                  @RequestParam("type") String type,
                                                  @RequestParam("param") String param) {
        return this.lockRun(() -> storageService.createStorage(supportCategory, description, type, param));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/storage/support/category/update")
    public ResultUtil<StorageModel> updateStorageSupportCategory(@RequestParam("storageId") int storageId, @RequestParam("supportCategory") int supportCategory) {
        return this.lockRun(() -> storageService.updateStorageSupportCategory(storageId, supportCategory));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/storage/register")
    public ResultUtil<StorageModel> registerStorage(@RequestParam("storageId") int storageId) {
        return this.lockRun(() -> storageService.registerStorage(storageId));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/storage/migrate")
    public ResultUtil<StorageModel> migrateStorage(@RequestParam("sourceStorageId") int sourceStorageId, @RequestParam("destStorageId") int destStorageId) {
        return this.lockRun(() -> storageService.migrateStorage(sourceStorageId, destStorageId));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/storage/maintenance")
    public ResultUtil<StorageModel> maintenanceStorage(@RequestParam("storageId") int storageId) {
        return this.lockRun(() -> storageService.maintenanceStorage(storageId));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @DeleteMapping("/api/storage/destroy")
    public ResultUtil<StorageModel> destroyStorage(@RequestParam("storageId") int storageId) {
        return this.lockRun(() -> storageService.destroyStorage(storageId));

    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @DeleteMapping("/api/storage/clear")
    public ResultUtil<StorageModel> clearUnLinkVolume(@RequestParam("storageId") int storageId) {
        return this.lockRun(() -> storageService.clearUnLinkVolume(storageId));

    }

}
