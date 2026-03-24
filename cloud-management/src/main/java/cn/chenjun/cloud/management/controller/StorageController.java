package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.common.util.BeanConverter;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
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
        List<StorageEntity> list = storageService.listStorage();
        List<SimpleStorageModel> models = BeanConverter.convert(list, SimpleStorageModel.class);
        return ResultUtil.success(models);
    }

    @GetMapping("/api/storage/search")
    public ResultUtil<Page<SimpleStorageModel>> search(@RequestParam(value = "storageType", required = false) Integer storageType,
                                                       @RequestParam(value = "storageStatus", required = false) Integer storageStatus,
                                                       @RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam("no") int no,
                                                       @RequestParam("size") int size) {
        Page<StorageEntity> page = storageService.search(storageType, storageStatus, keyword, no, size);

        return ResultUtil.success(Page.convert(page, source -> BeanConverter.convert(source, SimpleStorageModel.class)));
    }

    @GetMapping("/api/storage/info")
    public ResultUtil<StorageModel> getStorageInfo(@RequestParam("storageId") int storageId) {
        StorageEntity storage = storageService.getStorageInfo(storageId);
        return ResultUtil.success(this.convertService.initStorageModel(storage));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PutMapping("/api/storage/create")
    public ResultUtil<StorageModel> createStorage(@RequestParam("description") String description,
                                                  @RequestParam("supportCategory") int supportCategory,
                                                  @RequestParam("type") String type,
                                                  @RequestParam("param") String param) {
        StorageEntity storage = this.lockRun(() -> storageService.createStorage(supportCategory, description, type, param));
        return ResultUtil.success(this.convertService.initStorageModel(storage));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/storage/support/category/update")
    public ResultUtil<StorageModel> updateStorageSupportCategory(@RequestParam("storageId") int storageId, @RequestParam("supportCategory") int supportCategory) {
        StorageEntity storage = this.lockRun(() -> storageService.updateStorageSupportCategory(storageId, supportCategory));
        return ResultUtil.success(this.convertService.initStorageModel(storage));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/storage/register")
    public ResultUtil<StorageModel> registerStorage(@RequestParam("storageId") int storageId) {
        StorageEntity storage = this.lockRun(() -> storageService.registerStorage(storageId));
        return ResultUtil.success(this.convertService.initStorageModel(storage));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/storage/migrate")
    public ResultUtil<StorageModel> migrateStorage(@RequestParam("sourceStorageId") int sourceStorageId, @RequestParam("destStorageId") int destStorageId) {
        StorageEntity storage = this.lockRun(() -> storageService.migrateStorage(sourceStorageId, destStorageId));
        return ResultUtil.success(this.convertService.initStorageModel(storage));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/storage/maintenance")
    public ResultUtil<StorageModel> maintenanceStorage(@RequestParam("storageId") int storageId) {
        StorageEntity storage = this.lockRun(() -> storageService.maintenanceStorage(storageId));
        return ResultUtil.success(this.convertService.initStorageModel(storage));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @DeleteMapping("/api/storage/destroy")
    public ResultUtil<StorageModel> destroyStorage(@RequestParam("storageId") int storageId) {
        StorageEntity storage = this.lockRun(() -> storageService.destroyStorage(storageId));
        return ResultUtil.success(this.convertService.initStorageModel(storage));

    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @DeleteMapping("/api/storage/clear")
    public ResultUtil<StorageModel> clearUnLinkVolume(@RequestParam("storageId") int storageId) {
        StorageEntity storage = this.lockRun(() -> storageService.clearUnLinkVolume(storageId));
        return ResultUtil.success(this.convertService.initStorageModel(storage));

    }

}
