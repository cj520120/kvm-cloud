package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.management.model.CloneModel;
import cn.chenjun.cloud.management.model.MigrateModel;
import cn.chenjun.cloud.management.model.SimpleVolumeModel;
import cn.chenjun.cloud.management.model.VolumeModel;
import cn.chenjun.cloud.management.servcie.VolumeService;
import cn.chenjun.cloud.management.util.Constant;
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
        return this.lockRun(() -> this.volumeService.listVolumes());
    }

    @GetMapping("/api/volume/search")
    public ResultUtil<Page<SimpleVolumeModel>> search(@RequestParam(value = "storageId", required = false) Integer storageId,
                                                @RequestParam(value = "status",required = false) Integer status,
                                                @RequestParam(value = "templateId",required = false) Integer templateId,
                                                @RequestParam(value = "volumeType",required = false) String volumeType,
                                                @RequestParam(value = "keyword",required = false) String keyword,
                                                @RequestParam("no") int no,
                                                @RequestParam("size") int size) {
        return this.lockRun(() -> this.volumeService.search(storageId, status, templateId, volumeType, keyword, no, size));
    }
    @GetMapping("/api/volume/not/attach/all")
    public ResultUtil<List<SimpleVolumeModel>> listNoAttachVolumes(@RequestParam(value = "guestId", defaultValue = "0") int guestId) {
        return this.lockRun(() -> this.volumeService.listNoAttachVolumes(guestId));
    }

    @GetMapping("/api/volume/info")
    public ResultUtil<VolumeModel> getVolumeInfo(@RequestParam("volumeId") int volumeId) {
        return this.lockRun(() -> this.volumeService.getVolumeInfo(volumeId));
    }

    @PutMapping("/api/volume/create")
    public ResultUtil<VolumeModel> createVolume(@RequestParam("description") String description,
                                                @RequestParam("storageId") int storageId,
                                                @RequestParam("volumeSize") long volumeSize) {
        return this.lockRun(() -> this.volumeService.createVolume(description, storageId, 0, volumeSize * 1024 * 1024 * 1024));
    }

    @PutMapping("/api/volume/clone")
    public ResultUtil<CloneModel> cloneVolume(@RequestParam("description") String description,
                                              @RequestParam("sourceVolumeId") int sourceVolumeId,
                                              @RequestParam("storageId") int storageId) {
        return this.lockRun(() -> this.volumeService.cloneVolume(description, sourceVolumeId, storageId));
    }

    @PutMapping("/api/volume/migrate")
    public ResultUtil<MigrateModel> migrateVolume(
            @RequestParam("sourceVolumeId") int sourceVolumeId,
            @RequestParam("storageId") int storageId) {
        return this.lockRun(() -> this.volumeService.migrateVolume(sourceVolumeId, storageId));
    }

    @PostMapping("/api/volume/resize")
    public ResultUtil<VolumeModel> resizeVolume(
            @RequestParam("volumeId") int volumeId,
            @RequestParam("size") long size) {
        return this.lockRun(() -> this.volumeService.resizeVolume(volumeId, size * 1024 * 1024 * 1024));
    }

    @DeleteMapping("/api/volume/destroy")
    public ResultUtil<VolumeModel> destroyVolume(@RequestParam("volumeId") int volumeId) {
        return this.lockRun(() -> this.volumeService.destroyVolume(volumeId));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @PutMapping("/api/volume/template/create")
    public ResultUtil<VolumeModel> createVolumeTemplate(@RequestParam("volumeId") int volumeId,
                                                        @RequestParam("name") String name) {
        return this.lockRun(() -> volumeService.createVolumeTemplate(volumeId, name));


    }

    @DeleteMapping("/api/volume/destroy/batch")
    public ResultUtil<List<VolumeModel>> batchDestroyVolume(@RequestParam("volumeIds") String volumeIdsStr) {
        List<Integer> volumeIds = Arrays.stream(volumeIdsStr.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        return this.lockRun(() -> this.volumeService.batchDestroyVolume(volumeIds));
    }


}

