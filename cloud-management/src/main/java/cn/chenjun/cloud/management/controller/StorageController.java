package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.common.util.BeanConverter;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.model.*;
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
        List<StorageEntity> list = storageService.listStorage();
        List<SimpleStorageModel> models = BeanConverter.convert(list, SimpleStorageModel.class);
        return ResultUtil.success(models);
    }

    @GetMapping("/api/storage/search")
    public ResultUtil<Page<SimpleStorageModel>> search(@RequestParam(value = "type", required = false) Integer storageType,
                                                       @RequestParam(value = "status", required = false) Integer storageStatus,
                                                       @RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam("no") int no,
                                                       @RequestParam("size") int size) {
        Page<StorageEntity> page = storageService.search(storageType, storageStatus, keyword, no, size);

        return ResultUtil.success(Page.convert(page, source -> BeanConverter.convert(source, SimpleStorageModel.class)));
    }

    @GetMapping("/api/storage/children")
    public ResultUtil<List<StorageModel>> listChildrenStorage(@RequestParam(value = "storageId") Integer storageId) {
        List<StorageEntity> childrenStorages = storageService.listChildrenStorage(storageId);

        return ResultUtil.success(this.convertService.initStorageModel(childrenStorages));
    }

    @GetMapping("/api/storage/info")
    public ResultUtil<StorageModel> getStorageInfo(@RequestParam("storageId") int storageId) {
        StorageEntity storage = storageService.getStorageInfo(storageId);
        return ResultUtil.success(this.convertService.initStorageModel(storage));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PutMapping("/api/storage/create")
    public ResultUtil<StorageModel> createStorage(@RequestBody StorageCreateRequest request) {
        request.validate();
        StorageEntity storage = this.globalLockCall(() -> storageService.createStorage(request.getSupportCategory(), request.getDescription(), request.getType(), request.getParam()));
        return ResultUtil.success(this.convertService.initStorageModel(storage));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/storage/support/category/update")
    public ResultUtil<StorageModel> updateStorageSupportCategory(@RequestBody StorageUpdateSupportCategoryRequest request) {
        request.validate();
        StorageEntity storage = this.globalLockCall(() -> storageService.updateStorageSupportCategory(request.getStorageId(), request.getSupportCategory()));
        return ResultUtil.success(this.convertService.initStorageModel(storage));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/storage/register")
    public ResultUtil<StorageModel> registerStorage(@RequestBody StorageRegisterRequest request) {
        request.validate();
        StorageEntity storage = this.globalLockCall(() -> storageService.registerStorage(request.getStorageId()));
        return ResultUtil.success(this.convertService.initStorageModel(storage));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/storage/migrate")
    public ResultUtil<StorageModel> migrateStorage(@RequestBody StorageMigrateRequest request) {
        request.validate();
        StorageEntity storage = this.globalLockCall(() -> storageService.migrateStorage(request.getSourceStorageId(), request.getDestStorageId()));
        return ResultUtil.success(this.convertService.initStorageModel(storage));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/storage/maintenance")
    public ResultUtil<StorageModel> maintenanceStorage(@RequestBody StorageMaintenanceRequest request) {
        request.validate();
        StorageEntity storage = this.globalLockCall(() -> storageService.maintenanceStorage(request.getStorageId()));
        return ResultUtil.success(this.convertService.initStorageModel(storage));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @DeleteMapping("/api/storage/destroy")
    public ResultUtil<StorageModel> destroyStorage(@RequestBody StorageDestroyRequest request) {
        request.validate();
        StorageEntity storage = this.globalLockCall(() -> storageService.destroyStorage(request.getStorageId()));
        return ResultUtil.success(this.convertService.initStorageModel(storage));

    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @DeleteMapping("/api/storage/clear")
    public ResultUtil<StorageModel> clearUnLinkVolume(@RequestBody StorageClearRequest request) {
        request.validate();
        StorageEntity storage = this.globalLockCall(() -> storageService.clearUnLinkVolume(request.getStorageId()));
        return ResultUtil.success(this.convertService.initStorageModel(storage));

    }

}
