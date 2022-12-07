package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.model.CloneModel;
import cn.roamblue.cloud.management.model.MigrateModel;
import cn.roamblue.cloud.management.model.VolumeModel;
import cn.roamblue.cloud.management.servcie.VolumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class VolumeController {

    @Autowired
    private VolumeService volumeService;

    @GetMapping("/api/volume/all")
    public ResultUtil<List<VolumeModel>> listVolumes() {
        return this.volumeService.listVolumes();
    }

    @GetMapping("/api/volume/info")
    public ResultUtil<VolumeModel> getVolumeInfo(@RequestParam("volumeId") int volumeId) {
        return this.volumeService.getVolumeInfo(volumeId);
    }

    @PutMapping("/api/volume/create")
    public ResultUtil<VolumeModel> createVolume(@RequestParam("storageId") int storageId,
                                                @RequestParam("templateId") int templateId,
                                                @RequestParam("volumeType") String volumeType,
                                                @RequestParam("volumeSize") long volumeSize) {
        return this.volumeService.createVolume(storageId, templateId, volumeType, volumeSize);
    }

    @PutMapping("/api/volume/clone")
    public ResultUtil<CloneModel> cloneVolume(
            @RequestParam("sourceVolumeId") int sourceVolumeId,
            @RequestParam("storageId") int storageId,
            @RequestParam("volumeType") String volumeType) {
        return this.volumeService.cloneVolume(sourceVolumeId, storageId, volumeType);
    }

    @PutMapping("/api/volume/migrate")
    public ResultUtil<MigrateModel> migrateVolume(
            @RequestParam("sourceVolumeId") int sourceVolumeId,
            @RequestParam("storageId") int storageId,
            @RequestParam("volumeType") String volumeType) {
        return this.volumeService.migrateVolume(sourceVolumeId, storageId, volumeType);
    }

    @DeleteMapping("/api/volume/destroy")
    public ResultUtil<VolumeModel> destroyVolume(@RequestParam("volumeId") int volumeId) {
        return this.volumeService.destroyVolume(volumeId);
    }

}

