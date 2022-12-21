package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.model.*;
import cn.roamblue.cloud.management.servcie.GuestService;
import cn.roamblue.cloud.management.servcie.NetworkService;
import cn.roamblue.cloud.management.servcie.VolumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chenjun
 */
@RestController
public class GuestController {

    @Autowired
    private GuestService guestService;
    @Autowired
    private NetworkService networkService;
    @Autowired
    private VolumeService volumeService;

    @GetMapping("/api/guest/all")
    public ResultUtil<List<GuestModel>> listGuests() {
        return this.guestService.listGuests();
    }

    @GetMapping("/api/guest/info")
    public ResultUtil<GuestModel> getGuestInfo(@RequestParam("guestId") int guestId) {
        return this.guestService.getGuestInfo(guestId);
    }

    @GetMapping("/api/guest/vnc/password")
    public ResultUtil<String> getVncPassword(@RequestParam("guestId") int guestId) {
        return this.guestService.getVncPassword(guestId);
    }

    @GetMapping("/api/guest/network")
    public ResultUtil<List<GuestNetworkModel>> listGuestNetworks(@RequestParam("guestId") int guestId) {
        return this.networkService.listGuestNetworks(guestId);
    }

    @GetMapping("/api/guest/volume")
    public ResultUtil<List<VolumeModel>> listGuestVolumes(@RequestParam("guestId") int guestId) {
        return this.volumeService.listGuestVolumes(guestId);
    }

    @PutMapping("/api/guest/create")
    public ResultUtil<GuestModel> createGuest(@RequestParam("description") String description,
                                              @RequestParam("busType") String busType,
                                              @RequestParam("hostId") int hostId,
                                              @RequestParam("schemeId") int schemeId,
                                              @RequestParam("networkId") int networkId,
                                              @RequestParam("networkDeviceType") String networkDeviceType,
                                              @RequestParam("isoTemplateId") int isoTemplateId,
                                              @RequestParam("diskTemplateId") int diskTemplateId,
                                              @RequestParam("snapshotVolumeId") int snapshotVolumeId,
                                              @RequestParam("volumeId") int volumeId,
                                              @RequestParam("storageId") int storageId,
                                              @RequestParam("volumeType") String volumeType,
                                              @RequestParam("size") long size) {


        return this.guestService.createGuest(description, busType, hostId, schemeId, networkId, networkDeviceType, isoTemplateId, diskTemplateId, snapshotVolumeId, volumeId, storageId, volumeType, size);
    }

    @PostMapping("/api/guest/reinstall")
    public ResultUtil<GuestModel> reInstall(@RequestParam("guestId") int guestId,
                                            @RequestParam("isoTemplateId") int isoTemplateId,
                                            @RequestParam("diskTemplateId") int diskTemplateId,
                                            @RequestParam("snapshotVolumeId") int snapshotVolumeId,
                                            @RequestParam("volumeId") int volumeId,
                                            @RequestParam("storageId") int storageId,
                                            @RequestParam("volumeType") String volumeType,
                                            @RequestParam("size") long size) {


        return this.guestService.reInstall(guestId, isoTemplateId, diskTemplateId, snapshotVolumeId, volumeId, storageId, volumeType, size);
    }

    @PostMapping("/api/guest/start")
    public ResultUtil<GuestModel> start(@RequestParam("guestId") int guestId,
                                        @RequestParam("hostId") int hostId) {
        return this.guestService.start(guestId, hostId);

    }

    @PostMapping("/api/guest/reboot")
    public ResultUtil<GuestModel> reboot(@RequestParam("guestId") int guestId) {
        return this.guestService.reboot(guestId);
    }

    @PostMapping("/api/guest/shutdown")
    public ResultUtil<GuestModel> shutdown(@RequestParam("guestId") int guestId,
                                           @RequestParam("force") boolean force) {
        return this.guestService.shutdown(guestId, force);

    }

    @PostMapping("/api/guest/modify")
    public ResultUtil<GuestModel> modifyGuest(@RequestParam("guestId") int guestId,
                                              @RequestParam("description") String description,
                                              @RequestParam("busType") String busType,
                                              @RequestParam("cpu") int cpu,
                                              @RequestParam("memory") long memory) {

        return this.guestService.modifyGuest(guestId, description, busType, cpu, memory);
    }

    @PostMapping("/api/guest/cd/attach")
    public ResultUtil<GuestModel> attachCdRoom(@RequestParam("guestId") int guestId,
                                               @RequestParam("templateId") int templateId) {
        return this.guestService.attachCdRoom(guestId, templateId);
    }

    @PostMapping("/api/guest/cd/detach")
    public ResultUtil<GuestModel> detachCdRoom(@RequestParam("guestId") int guestId) {
        return this.guestService.detachCdRoom(guestId);
    }

    @PostMapping("/api/guest/disk/attach")
    public ResultUtil<AttachGuestVolumeModel> attachDisk(@RequestParam("guestId") int guestId,
                                                         @RequestParam("volumeId") int volumeId) {
        return this.guestService.attachDisk(guestId, volumeId);
    }

    @PostMapping("/api/guest/disk/detach")
    public ResultUtil<GuestModel> detachDisk(@RequestParam("guestId") int guestId,
                                             @RequestParam("guestDiskId") int guestDiskId) {
        return this.guestService.detachDisk(guestId, guestDiskId);
    }

    @PostMapping("/api/guest/network/attach")

    public ResultUtil<AttachGuestNetworkModel> attachNetwork(@RequestParam("guestId") int guestId,
                                                             @RequestParam("networkId") int networkId,
                                                             @RequestParam("driveType") String driveType) {
        return this.guestService.attachNetwork(guestId, networkId, driveType);

    }

    @PostMapping("/api/guest/network/detach")

    public ResultUtil<GuestModel> detachNetwork(@RequestParam("guestId") int guestId,
                                                @RequestParam("guestNetworkId") int guestNetworkId) {
        return this.guestService.detachNetwork(guestId, guestNetworkId);
    }

    @DeleteMapping("/api/guest/destroy")
    public ResultUtil<Void> destroyGuest(@RequestParam("guestId") int guestId) {
        return this.guestService.destroyGuest(guestId);
    }

}
