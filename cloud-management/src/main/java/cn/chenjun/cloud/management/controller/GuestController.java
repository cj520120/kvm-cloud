package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.annotation.LoginRequire;
import cn.chenjun.cloud.management.model.*;
import cn.chenjun.cloud.management.servcie.GuestService;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.servcie.VolumeService;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@LoginRequire
@RestController
public class GuestController extends BaseController {

    @Autowired
    private GuestService guestService;
    @Autowired
    private NetworkService networkService;
    @Autowired
    private VolumeService volumeService;

    @GetMapping("/api/guest/all")
    public ResultUtil<List<GuestModel>> listGuests() {
        return this.lockRun(() -> this.guestService.listGuests());
    }

    @GetMapping("/api/guest/system")
    public ResultUtil<List<GuestModel>> listSystemGuests(@RequestParam("networkId") int networkId) {
        return this.lockRun(() -> this.guestService.listSystemGuests(networkId));
    }

    @GetMapping("/api/guest/user")
    public ResultUtil<List<GuestModel>> listUserGuests() {
        return this.lockRun(() -> this.guestService.listUserGuests());
    }

    @GetMapping("/api/guest/info")
    public ResultUtil<GuestModel> getGuestInfo(@RequestParam("guestId") int guestId) {
        return this.lockRun(() -> this.guestService.getGuestInfo(guestId));
    }

    @GetMapping("/api/guest/vnc/password")
    public ResultUtil<String> getVncPassword(@RequestParam("guestId") int guestId) {
        return this.lockRun(() -> this.guestService.getVncPassword(guestId));
    }

    @GetMapping("/api/guest/network")
    public ResultUtil<List<GuestNetworkModel>> listGuestNetworks(@RequestParam("guestId") int guestId) {
        return this.lockRun(() -> this.networkService.listGuestNetworks(guestId));
    }

    @GetMapping("/api/guest/volume")
    public ResultUtil<List<VolumeModel>> listGuestVolumes(@RequestParam("guestId") int guestId) {
        return this.lockRun(() -> this.volumeService.listGuestVolumes(guestId));
    }

    @PutMapping("/api/guest/create")
    public ResultUtil<GuestModel> createGuest(@RequestParam("description") String description,
                                              @RequestParam(value = "systemCategory") int systemCategory,
                                              @RequestParam("bootstrapType") int bootstrapType,
                                              @RequestParam("busType") String busType,
                                              @RequestParam(value = "metaData", defaultValue = "{}") String metaData,
                                              @RequestParam(value = "userData", defaultValue = "{}") String userData,
                                              @RequestParam("groupId") int groupId,
                                              @RequestParam("hostId") int hostId,
                                              @RequestParam("schemeId") int schemeId,
                                              @RequestParam("networkId") int networkId,
                                              @RequestParam("networkDeviceType") String networkDeviceType,
                                              @RequestParam("isoTemplateId") int isoTemplateId,
                                              @RequestParam("diskTemplateId") int diskTemplateId,
                                              @RequestParam("volumeId") int volumeId,
                                              @RequestParam("storageId") int storageId,
                                              @RequestParam("size") long size) {
        Map<String, String> metaMap = GsonBuilderUtil.create().fromJson(metaData, new TypeToken<Map<String, String>>() {
        }.getType());
        Map<String, String> userMap = GsonBuilderUtil.create().fromJson(userData, new TypeToken<Map<String, String>>() {
        }.getType());
        return this.lockRun(() -> this.guestService.createGuest(groupId, description, systemCategory, bootstrapType, busType, hostId, schemeId, networkId, networkDeviceType, isoTemplateId, diskTemplateId, volumeId, storageId, metaMap, userMap, size * 1024 * 1024 * 1024));
    }

    @PostMapping("/api/guest/reinstall")
    public ResultUtil<GuestModel> reInstall(@RequestParam("guestId") int guestId,
                                            @RequestParam("systemCategory") int systemCategory,
                                            @RequestParam("bootstrapType") int bootstrapType,
                                            @RequestParam("isoTemplateId") int isoTemplateId,
                                            @RequestParam("diskTemplateId") int diskTemplateId,
                                            @RequestParam("volumeId") int volumeId,
                                            @RequestParam("storageId") int storageId,
                                            @RequestParam(value = "metaData", defaultValue = "{}") String metaData,
                                            @RequestParam(value = "userData", defaultValue = "{}") String userData,
                                            @RequestParam("size") long size) {

        Map<String, String> metaMap = GsonBuilderUtil.create().fromJson(metaData, new TypeToken<Map<String, String>>() {
        }.getType());
        Map<String, String> userMap = GsonBuilderUtil.create().fromJson(userData, new TypeToken<Map<String, String>>() {
        }.getType());

        return this.lockRun(() -> this.guestService.reInstall(guestId, systemCategory, bootstrapType, metaMap, userMap, isoTemplateId, diskTemplateId, volumeId, storageId, size * 1024 * 1024 * 1024));
    }

    @PostMapping("/api/guest/start/batch")
    public ResultUtil<List<GuestModel>> batchStart(@RequestParam("guestIds") String guestIdsStr) {
        List<Integer> guestIds = Arrays.stream(guestIdsStr.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        return this.lockRun(() -> this.guestService.batchStart(guestIds));
    }

    @PostMapping("/api/guest/start")
    public ResultUtil<GuestModel> start(@RequestParam("guestId") int guestId,
                                        @RequestParam("hostId") int hostId) {
        return this.lockRun(() -> this.guestService.start(guestId, hostId));

    }

    @PostMapping("/api/guest/reboot")
    public ResultUtil<GuestModel> reboot(@RequestParam("guestId") int guestId) {
        return this.lockRun(() -> this.guestService.reboot(guestId));
    }

    @PostMapping("/api/guest/migrate")
    public ResultUtil<GuestModel> migrate(@RequestParam("guestId") int guestId, @RequestParam("hostId") int hostId) {
        return this.lockRun(() -> this.guestService.migrate(guestId, hostId));
    }

    @PostMapping("/api/guest/shutdown/batch")
    public ResultUtil<List<GuestModel>> batchStop(@RequestParam("guestIds") String guestIdsStr) {
        List<Integer> guestIds = Arrays.stream(guestIdsStr.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        return this.lockRun(() -> this.guestService.batchStop(guestIds));
    }

    @PostMapping("/api/guest/shutdown")
    public ResultUtil<GuestModel> shutdown(@RequestParam("guestId") int guestId,
                                           @RequestParam("force") boolean force) {
        return this.lockRun(() -> this.guestService.shutdown(guestId, force));

    }

    @PostMapping("/api/guest/cd/attach")
    public ResultUtil<GuestModel> attachCdRoom(@RequestParam("guestId") int guestId,
                                               @RequestParam("templateId") int templateId) {
        return this.lockRun(() -> this.guestService.attachCdRoom(guestId, templateId));
    }

    @PostMapping("/api/guest/cd/detach")
    public ResultUtil<GuestModel> detachCdRoom(@RequestParam("guestId") int guestId) {
        return this.lockRun(() -> this.guestService.detachCdRoom(guestId));
    }

    @PostMapping("/api/guest/disk/attach")
    public ResultUtil<AttachGuestVolumeModel> attachDisk(@RequestParam("guestId") int guestId,
                                                         @RequestParam("volumeId") int volumeId) {
        return this.lockRun(() -> this.guestService.attachDisk(guestId, volumeId));
    }

    @PostMapping("/api/guest/disk/detach")
    public ResultUtil<GuestModel> detachDisk(@RequestParam("guestId") int guestId,
                                             @RequestParam("guestDiskId") int guestDiskId) {
        return this.lockRun(() -> this.guestService.detachDisk(guestId, guestDiskId));
    }

    @PostMapping("/api/guest/network/attach")

    public ResultUtil<AttachGuestNetworkModel> attachNetwork(@RequestParam("guestId") int guestId,
                                                             @RequestParam("networkId") int networkId,
                                                             @RequestParam("driveType") String driveType) {
        return this.lockRun(() -> this.guestService.attachNetwork(guestId, networkId, driveType));

    }

    @PostMapping("/api/guest/network/detach")

    public ResultUtil<GuestModel> detachNetwork(@RequestParam("guestId") int guestId,
                                                @RequestParam("guestNetworkId") int guestNetworkId) {
        return this.lockRun(() -> this.guestService.detachNetwork(guestId, guestNetworkId));
    }

    @PostMapping("/api/guest/modify")
    public ResultUtil<GuestModel> updateGuest(@RequestParam("guestId") int guestId,
                                              @RequestParam("systemCategory") int systemCategory,
                                              @RequestParam("bootstrapType") int bootstrapType,
                                              @RequestParam("busType") String busType,
                                              @RequestParam("description") String description,
                                              @RequestParam("schemeId") int schemeId,
                                              @RequestParam("groupId") int groupId) {
        return this.lockRun(() -> this.guestService.modifyGuest(guestId, systemCategory, bootstrapType, groupId, busType, description, schemeId));
    }

    @DeleteMapping("/api/guest/destroy")
    public ResultUtil<GuestModel> destroyGuest(@RequestParam("guestId") int guestId) {
        return this.lockRun(() -> this.guestService.destroyGuest(guestId));
    }

}
