package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.annotation.LoginRequire;
import cn.chenjun.cloud.management.annotation.PermissionRequire;
import cn.chenjun.cloud.management.model.*;
import cn.chenjun.cloud.management.servcie.GuestService;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.servcie.VolumeService;
import cn.chenjun.cloud.management.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
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
    public ResultUtil<List<SimpleGuestModel>> listGuests() {
        return this.lockRun(() -> this.guestService.listGuests());
    }

    @GetMapping("/api/guest/search")
    public ResultUtil<Page<SimpleGuestModel>> search(@RequestParam(value = "guestType", required = false) Integer guestType,
                                               @RequestParam(value = "groupId",required = false) Integer groupId,
                                               @RequestParam(value = "networkId",required = false) Integer networkId,
                                               @RequestParam(value = "hostId",required = false) Integer hostId,
                                               @RequestParam(value = "schemeId",required = false) Integer schemeId,
                                               @RequestParam(value = "status",required = false) Integer status,
                                               @RequestParam(value = "keyword",required = false) String keyword,
                                               @RequestParam("no") int no,
                                               @RequestParam("size") int size) {
        return this.lockRun(() -> this.guestService.search(guestType, groupId, networkId, hostId, schemeId, status, keyword, no, size));
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

    @GetMapping("/api/guest/disk")
    public ResultUtil<List<VolumeModel>> listGuestVolumes(@RequestParam("guestId") int guestId) {
        return this.lockRun(() -> this.volumeService.listGuestVolumes(guestId));
    }

    
    @PutMapping("/api/guest/create")
    public ResultUtil<GuestModel> createGuest(@RequestParam("description") String description,
                                              @RequestParam("category") int systemCategory,
                                              @RequestParam("bootstrapType") int bootstrapType,
                                              @RequestParam("bootDeviceDriver") String deviceBus,
                                              @RequestParam("schemeId") int schemeId,
                                              @RequestParam("networkId") int networkId,
                                              @RequestParam("networkDeviceDriver") String networkDeviceType,
                                              @RequestParam(value = "hostId", defaultValue = "0") int hostId,
                                              @RequestParam(value = "isoTemplateId", defaultValue = "0") int isoTemplateId,
                                              @RequestParam(value = "diskTemplateId", defaultValue = "0") int diskTemplateId,
                                              @RequestParam(value = "volumeId", defaultValue = "0") int volumeId,
                                              @RequestParam(value = "storageId", defaultValue = "0") int storageId,
                                              @RequestParam(value = "diskSize", defaultValue = "0") long size,
                                              @RequestParam(value = "groupId", defaultValue = "0") int groupId,
                                              @RequestParam(value = "hostname", defaultValue = "") String hostName,
                                              @RequestParam(value = "password", defaultValue = "") String password,
                                              @RequestParam(value = "sshId", defaultValue = "0") int sshId) {
        Map<String, String> metaMap = new HashMap<>();
        Map<String, String> userMap = new HashMap<>();
        if (!ObjectUtils.isEmpty(hostName)) {
            metaMap.put("hostname", hostName);
            metaMap.put("local-hostname", hostName);
        }
        if (!ObjectUtils.isEmpty(password)) {
            userMap.put("password", password);
        }
        if (sshId > 0) {
            userMap.put("sshId", String.valueOf(sshId));
        }
        userMap.put("sshId", String.valueOf(sshId));
        return this.lockRun(() -> this.guestService.createGuest(groupId, description, systemCategory, bootstrapType, deviceBus, hostId, schemeId, networkId, networkDeviceType, isoTemplateId, diskTemplateId, volumeId, storageId, metaMap, userMap, size * 1024 * 1024 * 1024));
    }

    
    @PostMapping("/api/guest/reinstall")
    public ResultUtil<GuestModel> reInstall(@RequestParam("guestId") int guestId,
                                            @RequestParam("category") int systemCategory,
                                            @RequestParam("bootstrapType") int bootstrapType,
                                            @RequestParam("bootDeviceDriver") String deviceBus,
                                            @RequestParam(value = "isoTemplateId", defaultValue = "0") int isoTemplateId,
                                            @RequestParam(value = "diskTemplateId", defaultValue = "0") int diskTemplateId,
                                            @RequestParam(value = "volumeId", defaultValue = "0") int volumeId,
                                            @RequestParam(value = "storageId", defaultValue = "0") int storageId,
                                            @RequestParam(value = "diskSize", defaultValue = "0") long size) {


        return this.lockRun(() -> this.guestService.reInstall(guestId, deviceBus, systemCategory, bootstrapType, isoTemplateId, diskTemplateId, volumeId, storageId, size * 1024 * 1024 * 1024));
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
                                           @RequestParam(value = "force", required = false) boolean force) {
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

    @PostMapping("/api/guest/disk/modify")
    public ResultUtil<VolumeModel> modifyGuestDiskDeviceType(@RequestParam("guestId") int guestId,
                                                             @RequestParam("deviceId") int deviceId,
                                                             @RequestParam("driver") String deviceBus) {
        return this.lockRun(() -> this.guestService.modifyGuestDiskDeviceType(guestId, deviceId, deviceBus));
    }

    @PostMapping("/api/guest/disk/attach")
    public ResultUtil<AttachGuestVolumeModel> attachDisk(@RequestParam("guestId") int guestId,
                                                         @RequestParam("volumeId") int volumeId,
                                                         @RequestParam("diskDriver") String diskDriver) {
        return this.lockRun(() -> this.guestService.attachDisk(guestId, volumeId, diskDriver));
    }

    @PostMapping("/api/guest/disk/detach")
    public ResultUtil<GuestModel> detachDisk(@RequestParam("guestId") int guestId,
                                             @RequestParam("guestDiskId") int guestDiskId) {
        return this.lockRun(() -> this.guestService.detachDisk(guestId, guestDiskId));
    }

    @PostMapping("/api/guest/network/attach")

    public ResultUtil<AttachGuestNetworkModel> attachNetwork(@RequestParam("guestId") int guestId,
                                                             @RequestParam("networkId") int networkId,
                                                             @RequestParam("driver") String driveType) {
        return this.lockRun(() -> this.guestService.attachNetwork(guestId, networkId, driveType));

    }

    @PostMapping("/api/guest/network/detach")
    public ResultUtil<GuestModel> detachNetwork(@RequestParam("guestId") int guestId,
                                                @RequestParam("guestNetworkId") int guestNetworkId) {
        return this.lockRun(() -> this.guestService.detachNetwork(guestId, guestNetworkId));
    }
    @PostMapping("/api/guest/modify")
    public ResultUtil<GuestModel> updateGuest(@RequestParam("guestId") int guestId,
                                              @RequestParam("category") int systemCategory,
                                              @RequestParam("bootstrapType") int bootstrapType,
                                              @RequestParam("description") String description,
                                              @RequestParam("schemeId") int schemeId,
                                              @RequestParam("groupId") int groupId) {
        return this.lockRun(() -> this.guestService.modifyGuest(guestId, systemCategory, bootstrapType, groupId, description, schemeId));
    }

    
    @DeleteMapping("/api/guest/destroy")
    public ResultUtil<GuestModel> destroyGuest(@RequestParam("guestId") int guestId) {
        return this.lockRun(() -> this.guestService.destroyGuest(guestId));
    }

}
