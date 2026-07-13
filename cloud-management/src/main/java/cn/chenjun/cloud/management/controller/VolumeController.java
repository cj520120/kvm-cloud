package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.model.*;
import cn.chenjun.cloud.management.servcie.VolumeService;
import cn.chenjun.cloud.management.servcie.bean.CloneInfo;
import cn.chenjun.cloud.management.servcie.bean.MigrateVolumeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chenjun
 */
@RestController
public class VolumeController extends BaseController {

    @Autowired
    private VolumeService volumeService;

    @GetMapping("/api/volume/all")
    public ResultUtil<List<SimpleVolumeModel>> listVolumes() {
        List<VolumeEntity> volumes = this.volumeService.listVolumes();

        List<SimpleVolumeModel> models = this.convertService.initSimpleVolumeModels(volumes);
        return ResultUtil.success(models);
    }

    @GetMapping("/api/volume/search")
    public ResultUtil<Page<SimpleVolumeModel>> search(@RequestParam(value = "storageId", required = false) Integer storageId,
                                                      @RequestParam(value = "status", required = false) Integer status,
                                                      @RequestParam(value = "templateId", required = false) Integer templateId,
                                                      @RequestParam(value = "volumeType", required = false) String volumeType,
                                                      @RequestParam(value = "keyword", required = false) String keyword,
                                                      @RequestParam("no") int no,
                                                      @RequestParam("size") int size) {
        Page<VolumeEntity> page = this.volumeService.search(storageId, status, templateId, volumeType, keyword, no, size);
        Page<SimpleVolumeModel> model = Page.create(page);
        model.setList(this.convertService.initSimpleVolumeModels(page.getList()));
        return ResultUtil.success(model);
    }

    @GetMapping("/api/volume/not/attach/all")
    public ResultUtil<List<SimpleVolumeModel>> listNoAttachVolumes(@RequestParam(value = "guestId", defaultValue = "0") int guestId) {
        List<VolumeEntity> volumes = this.volumeService.listNoAttachVolumes(guestId);

        List<SimpleVolumeModel> models = this.convertService.initSimpleVolumeModels(volumes);
        return ResultUtil.success(models);
    }

    @GetMapping("/api/volume/info")
    public ResultUtil<VolumeModel> getVolumeInfo(@RequestParam("volumeId") int volumeId) {
        VolumeEntity volume = this.volumeService.getVolumeById(volumeId);
        return ResultUtil.success(this.convertService.initVolumeModel(volume));
    }

    @PutMapping("/api/volume/create")
    public ResultUtil<VolumeModel> createVolume(@RequestBody VolumeCreateRequest request) {
        request.validate();
        VolumeEntity volume = this.globalLockCall(() -> this.volumeService.createVolume(request.getDescription(), request.getStorageId(), 0, request.getVolumeSize() * 1024 * 1024 * 1024));
        return ResultUtil.success(this.convertService.initVolumeModel(volume));
    }

    @PutMapping("/api/volume/clone")
    public ResultUtil<CloneModel> cloneVolume(@RequestBody VolumeCloneRequest request) {
        request.validate();
        CloneInfo info = this.globalLockCall(() -> this.volumeService.cloneVolume(request.getDescription(), request.getSourceVolumeId(), request.getStorageId()));
        CloneModel model = CloneModel.builder().source(this.convertService.initVolumeModel(info.getSource())).clone(this.convertService.initVolumeModel(info.getClone())).build();
        return ResultUtil.success(model);
    }

    @PutMapping("/api/volume/migrate")
    public ResultUtil<MigrateModel> migrateVolume(@RequestBody VolumeMigrateRequest request) {
        request.validate();
        MigrateVolumeInfo info = this.globalLockCall(() -> this.volumeService.migrateVolume(request.getSourceVolumeId(), request.getStorageId()));
        MigrateModel model = MigrateModel.builder().source(this.convertService.initVolumeModel(info.getSource())).migrate(this.convertService.initVolumeModel(info.getMigrate())).build();
        return ResultUtil.success(model);
    }

    @PostMapping("/api/volume/resize")
    public ResultUtil<VolumeModel> resizeVolume(@RequestBody VolumeResizeRequest request) {
        request.validate();
        VolumeEntity volume = this.globalLockCall(() -> this.volumeService.resizeVolume(request.getVolumeId(), request.getSize() * 1024 * 1024 * 1024));
        return ResultUtil.success(this.convertService.initVolumeModel(volume));
    }

    @DeleteMapping("/api/volume/destroy")
    public ResultUtil<VolumeModel> destroyVolume(@RequestBody VolumeDestroyRequest request) {
        request.validate();
        VolumeEntity volume = this.globalLockCall(() -> this.volumeService.destroyVolume(request.getVolumeId()));
        return ResultUtil.success(this.convertService.initVolumeModel(volume));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @PutMapping("/api/volume/template/create")
    public ResultUtil<VolumeModel> createVolumeTemplate(@RequestBody VolumeTemplateCreateRequest request) {
        request.validate();
        VolumeEntity volume = this.globalLockCall(() -> volumeService.createVolumeTemplate(request.getVolumeId(), request.getName(), request.getArch()));
        return ResultUtil.success(this.convertService.initVolumeModel(volume));


    }

    @DeleteMapping("/api/volume/destroy/batch")
    public ResultUtil<List<SimpleVolumeModel>> batchDestroyVolume(@RequestBody VolumeBatchDestroyRequest request) {
        request.validate();
        List<VolumeEntity> volumes = this.globalLockCall(() -> this.volumeService.batchDestroyVolume(request.getVolumeIds()));
        List<SimpleVolumeModel> models = this.convertService.initSimpleVolumeModels(volumes);
        return ResultUtil.success(models);
    }


}

