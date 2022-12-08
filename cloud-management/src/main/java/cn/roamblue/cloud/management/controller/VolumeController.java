package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.model.CloneModel;
import cn.roamblue.cloud.management.model.MigrateModel;
import cn.roamblue.cloud.management.model.SnapshotModel;
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
                                                @RequestParam("snapshotVolumeId") int snapshotVolumeId,
                                                @RequestParam("volumeType") String volumeType,
                                                @RequestParam("volumeSize") long volumeSize) {
        return this.volumeService.createVolume(storageId, templateId,snapshotVolumeId, volumeType, volumeSize);
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
    @PostMapping("/api/volume/resize")
    public ResultUtil<VolumeModel> resizeVolume(
            @RequestParam("volumeId") int volumeId,
            @RequestParam("size") long size) {
        return this.volumeService.resizeVolume(volumeId, size);
    }
    @DeleteMapping("/api/volume/destroy")
    public ResultUtil<VolumeModel> destroyVolume(@RequestParam("volumeId") int volumeId) {
        return this.volumeService.destroyVolume(volumeId);
    }
    @GetMapping("/api/snapshot/all")
    public ResultUtil<List<SnapshotModel>> listSnapshot(){
        return this.volumeService.listSnapshot();
    }

    @GetMapping("/api/snapshot/info")
    public ResultUtil<SnapshotModel> getSnapshotInfo(@RequestParam("snapshotVolumeId") int snapshotVolumeId){
        return this.volumeService.getSnapshotInfo(snapshotVolumeId);
    }
    @PutMapping("/api/snapshot/create")
    public ResultUtil<SnapshotModel> createVolumeSnapshot(@RequestParam("volumeId") int volumeId,
                                                          @RequestParam("snapshotName") String snapshotName,
                                                          @RequestParam("snapshotVolumeType") String snapshotVolumeType) {
        return this.volumeService.createVolumeSnapshot(volumeId, snapshotName, snapshotVolumeType);
    }
    @DeleteMapping("/api/snapshot/destroy")
    public ResultUtil<SnapshotModel> destroySnapshot(@RequestParam("") int snapshotVolumeId) {
        return this.volumeService.destroySnapshot(snapshotVolumeId);
    }
}

