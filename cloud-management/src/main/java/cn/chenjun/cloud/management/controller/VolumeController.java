package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.model.CloneModel;
import cn.chenjun.cloud.management.model.MigrateModel;
import cn.chenjun.cloud.management.model.SimpleVolumeModel;
import cn.chenjun.cloud.management.model.VolumeModel;
import cn.chenjun.cloud.management.servcie.VolumeService;
import cn.chenjun.cloud.management.servcie.bean.CloneInfo;
import cn.chenjun.cloud.management.servcie.bean.MigrateVolumeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    public ResultUtil<VolumeModel> createVolume(@RequestParam("description") String description,
                                                @RequestParam("storageId") int storageId,
                                                @RequestParam("volumeSize") long volumeSize) {
        VolumeEntity volume = this.lockRun(() -> this.volumeService.createVolume(description, storageId, 0, volumeSize * 1024 * 1024 * 1024));
        return ResultUtil.success(this.convertService.initVolumeModel(volume));
    }

    @PutMapping("/api/volume/clone")
    public ResultUtil<CloneModel> cloneVolume(@RequestParam("description") String description,
                                              @RequestParam("sourceVolumeId") int sourceVolumeId,
                                              @RequestParam("storageId") int storageId) {
        CloneInfo info = this.lockRun(() -> this.volumeService.cloneVolume(description, sourceVolumeId, storageId));
        CloneModel model = CloneModel.builder().source(this.convertService.initVolumeModel(info.getSource())).clone(this.convertService.initVolumeModel(info.getClone())).build();
        return ResultUtil.success(model);
    }

    @PutMapping("/api/volume/migrate")
    public ResultUtil<MigrateModel> migrateVolume(
            @RequestParam("sourceVolumeId") int sourceVolumeId,
            @RequestParam("storageId") int storageId) {
        MigrateVolumeInfo info = this.lockRun(() -> this.volumeService.migrateVolume(sourceVolumeId, storageId));
        MigrateModel model = MigrateModel.builder().source(this.convertService.initVolumeModel(info.getSource())).migrate(this.convertService.initVolumeModel(info.getMigrate())).build();
        return ResultUtil.success(model);
    }

    @PostMapping("/api/volume/resize")
    public ResultUtil<VolumeModel> resizeVolume(
            @RequestParam("volumeId") int volumeId,
            @RequestParam("size") long size) {
        VolumeEntity volume = this.lockRun(() -> this.volumeService.resizeVolume(volumeId, size * 1024 * 1024 * 1024));
        return ResultUtil.success(this.convertService.initVolumeModel(volume));
    }

    @DeleteMapping("/api/volume/destroy")
    public ResultUtil<VolumeModel> destroyVolume(@RequestParam("volumeId") int volumeId) {
        VolumeEntity volume = this.lockRun(() -> this.volumeService.destroyVolume(volumeId));
        return ResultUtil.success(this.convertService.initVolumeModel(volume));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @PutMapping("/api/volume/template/create")
    public ResultUtil<VolumeModel> createVolumeTemplate(@RequestParam("volumeId") int volumeId,
                                                        @RequestParam("name") String name,
                                                        @RequestParam("arch") String arch) {
        VolumeEntity volume = this.lockRun(() -> volumeService.createVolumeTemplate(volumeId, name, arch));
        return ResultUtil.success(this.convertService.initVolumeModel(volume));


    }

    @DeleteMapping("/api/volume/destroy/batch")
    public ResultUtil<List<SimpleVolumeModel>> batchDestroyVolume(@RequestParam("volumeIds") String volumeIdsStr) {
        List<Integer> volumeIds = Arrays.stream(volumeIdsStr.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<VolumeEntity> volumes = this.lockRun(() -> this.volumeService.batchDestroyVolume(volumeIds));
        List<SimpleVolumeModel> models = this.convertService.initSimpleVolumeModels(volumes);
        return ResultUtil.success(models);
    }


}

