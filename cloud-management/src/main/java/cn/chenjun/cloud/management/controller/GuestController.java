package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.BeanConverter;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.model.*;
import cn.chenjun.cloud.management.servcie.GuestService;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.servcie.VolumeService;
import cn.chenjun.cloud.management.servcie.bean.AttachNicInfo;
import cn.chenjun.cloud.management.servcie.bean.AttachVolumeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
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
        List<GuestEntity> entityList = this.guestService.listGuests();
        return ResultUtil.success(this.convertService.initGuestList(entityList));
    }

    @GetMapping("/api/guest/search")
    public ResultUtil<Page<GuestModel>> search(@RequestParam(value = "guestType", required = false) Integer guestType,
                                                     @RequestParam(value = "groupId", required = false) Integer groupId,
                                                     @RequestParam(value = "networkId", required = false) Integer networkId,
                                                     @RequestParam(value = "hostId", required = false) Integer hostId,
                                                     @RequestParam(value = "schemeId", required = false) Integer schemeId,
                                                     @RequestParam(value = "status", required = false) Integer status,
                                                     @RequestParam(value = "keyword", required = false) String keyword,
                                                     @RequestParam("no") int no,
                                                     @RequestParam("size") int size) {
        Page<GuestEntity> page = this.guestService.search(guestType, groupId, networkId, hostId, schemeId, status, keyword, no, size);

        List<GuestModel> models = this.convertService.initGuestList(page.getList());
        Page<GuestModel> pageModels = Page.create(page, models);
        return ResultUtil.success(pageModels);

    }

    @GetMapping("/api/guest/info")
    public ResultUtil<GuestModel> getGuestInfo(@RequestParam("guestId") int guestId) {
        GuestEntity guest = this.guestService.getGuestInfo(guestId);
        return ResultUtil.success(this.convertService.initGuestModel(guest));
    }

    @GetMapping("/api/guest/vnc/password")
    public ResultUtil<String> getVncPassword(@RequestParam("guestId") int guestId) {
        String password = this.guestService.getVncPassword(guestId);
        return ResultUtil.success(password);
    }

    @GetMapping("/api/guest/network")
    public ResultUtil<List<GuestNetworkModel>> listGuestNetworks(@RequestParam("guestId") int guestId) {
        List<GuestNetworkEntity> nics = this.networkService.listGuestNetworks(guestId);
        List<GuestNetworkModel> models = BeanConverter.convert(nics, GuestNetworkModel.class);
        return ResultUtil.success(models);
    }

    @GetMapping("/api/guest/disk")
    public ResultUtil<List<VolumeModel>> listGuestVolumes(@RequestParam("guestId") int guestId) {
        List<VolumeEntity> volumes = this.volumeService.listGuestVolumes(guestId);
        List<VolumeModel> models = volumes.stream().map(this.convertService::initVolumeModel).collect(Collectors.toList());
        return ResultUtil.success(models);
    }


    @PutMapping("/api/guest/create")
    public ResultUtil<GuestModel> createGuest(@RequestParam("description") String description,
                                              @RequestParam("category") int systemCategory,
                                              @RequestParam("bootstrapType") int bootstrapType,
                                              @RequestParam("bootDeviceDriver") String deviceBus,
                                              @RequestParam("schemeId") int schemeId,
                                              @RequestParam("networkId") int networkId,
                                              @RequestParam("arch") String arch,
                                              @RequestParam("networkDeviceDriver") String networkDeviceType,
                                              @RequestParam(value = "bindHostId", defaultValue = "0") int bindHostId,
                                              @RequestParam(value = "hostId", defaultValue = "0") int hostId,
                                              @RequestParam(value = "isoTemplateId", defaultValue = "0") int isoTemplateId,
                                              @RequestParam(value = "diskTemplateId", defaultValue = "0") int diskTemplateId,
                                              @RequestParam(value = "volumeId", defaultValue = "0") int volumeId,
                                              @RequestParam(value = "storageId", defaultValue = "0") int storageId,
                                              @RequestParam(value = "diskSize", defaultValue = "0") long size,
                                              @RequestParam(value = "groupId", defaultValue = "0") int groupId,
                                              @RequestParam(value = "hostname", defaultValue = "") String hostName,
                                              @RequestParam(value = "password", defaultValue = "") String password,
                                              @RequestParam(value = "sshId", defaultValue = "0") int sshId,
                                              @RequestParam(value = "initVendorData", defaultValue = "") String initVendorData) {
        if (StringUtils.isEmpty(description)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的描述信息");
        }
        if (StringUtils.isEmpty(deviceBus)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择磁盘驱动");
        }
        if (StringUtils.isEmpty(networkDeviceType)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择网卡驱动");
        }
        if (isoTemplateId <= 0 && diskTemplateId <= 0 && volumeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择系统来源");
        }
        if (schemeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择架构方案");
        }
        if (isoTemplateId > 0 && size <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入磁盘大小");
        }
        GuestEntity guest = this.globalLockCall(() -> this.guestService.createGuest(groupId, description, systemCategory, bootstrapType, deviceBus, bindHostId, hostId, schemeId, networkId, networkDeviceType, isoTemplateId, diskTemplateId, volumeId, storageId, size * 1024 * 1024 * 1024, hostName, password, sshId, arch, initVendorData));
        return ResultUtil.success(this.convertService.initGuestModel(guest));
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


        GuestEntity guest = this.globalLockCall(() -> this.guestService.reInstall(guestId, deviceBus, systemCategory, bootstrapType, isoTemplateId, diskTemplateId, volumeId, storageId, size * 1024 * 1024 * 1024));
        return ResultUtil.success(this.convertService.initGuestModel(guest));
    }

    @PostMapping("/api/guest/start/batch")
    public ResultUtil<List<GuestModel>> batchStart(@RequestParam("guestIds") String guestIdsStr) {
        List<Integer> guestIds = Arrays.stream(guestIdsStr.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<GuestEntity> guests = this.globalLockCall(() -> this.guestService.batchStart(guestIds));
        List<GuestModel> models = this.convertService.initGuestList(guests);
        return ResultUtil.success(models);
    }

    @PostMapping("/api/guest/shutdown/batch")
    public ResultUtil<List<GuestModel>> batchStop(@RequestParam("guestIds") String guestIdsStr) {
        List<Integer> guestIds = Arrays.stream(guestIdsStr.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<GuestEntity> guests = this.globalLockCall(() -> this.guestService.batchStop(guestIds));
        List<GuestModel> models = this.convertService.initGuestList(guests);
        return ResultUtil.success(models);
    }

    @PostMapping("/api/guest/start")
    public ResultUtil<GuestModel> start(@RequestParam("guestId") int guestId,
                                        @RequestParam("hostId") int hostId) {
        GuestEntity guest = this.globalLockCall(() -> this.guestService.start(guestId, hostId));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);

    }

    @PostMapping("/api/guest/reboot")
    public ResultUtil<GuestModel> reboot(@RequestParam("guestId") int guestId) {
        GuestEntity guest = this.globalLockCall(() -> this.guestService.reboot(guestId));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }

    @PostMapping("/api/guest/migrate")
    public ResultUtil<GuestModel> migrate(@RequestParam("guestId") int guestId, @RequestParam("hostId") int hostId) {
        GuestEntity guest = this.globalLockCall(() -> this.guestService.migrate(guestId, hostId));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }


    @PostMapping("/api/guest/shutdown")
    public ResultUtil<GuestModel> shutdown(@RequestParam("guestId") int guestId,
                                           @RequestParam(value = "force", required = false) boolean force) {
        GuestEntity guest = this.globalLockCall(() -> this.guestService.shutdown(guestId, force));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);

    }

    @PostMapping("/api/guest/cd/attach")
    public ResultUtil<GuestModel> attachCdRoom(@RequestParam("guestId") int guestId,
                                               @RequestParam("templateId") int templateId) {
        GuestEntity guest = this.globalLockCall(() -> this.guestService.attachCdRoom(guestId, templateId));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }

    @PostMapping("/api/guest/cd/detach")
    public ResultUtil<GuestModel> detachCdRoom(@RequestParam("guestId") int guestId) {
        GuestEntity guest = this.globalLockCall(() -> this.guestService.detachCdRoom(guestId));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }

    @PostMapping("/api/guest/disk/modify")
    public ResultUtil<VolumeModel> modifyGuestDiskDeviceType(@RequestParam("guestId") int guestId,
                                                             @RequestParam("deviceId") int deviceId,
                                                             @RequestParam("driver") String deviceBus) {
        VolumeEntity volume = this.globalLockCall(() -> this.guestService.modifyGuestDiskDeviceType(guestId, deviceId, deviceBus));
        return ResultUtil.success(this.convertService.initVolumeModel(volume));
    }

    @PutMapping("/api/guest/disk/create")
    public ResultUtil<VolumeModel> createVolume(@RequestParam("guestId") int guestId,
                                                @RequestParam("diskDriver") String diskDriver,
                                                @RequestParam("description") String description,
                                                @RequestParam("storageId") int storageId,
                                                @RequestParam("volumeSize") long volumeSize) {
        VolumeEntity volume = this.globalLockCall(() -> this.guestService.createDisk(guestId, diskDriver, description, storageId, volumeSize * 1024 * 1024 * 1024));
        return ResultUtil.success(this.convertService.initVolumeModel(volume));

    }

    @PostMapping("/api/guest/disk/attach")
    public ResultUtil<AttachGuestVolumeModel> attachDisk(@RequestParam("guestId") int guestId,
                                                         @RequestParam("volumeId") int volumeId,
                                                         @RequestParam("diskDriver") String diskDriver) {
        AttachVolumeInfo attach = this.globalLockCall(() -> this.guestService.attachDisk(guestId, volumeId, diskDriver));
        AttachGuestVolumeModel model = new AttachGuestVolumeModel();
        model.setGuest(this.convertService.initGuestModel(attach.getGuest()));
        model.setVolume(this.convertService.initVolumeModel(attach.getVolume()));
        return ResultUtil.success(model);
    }

    @PostMapping("/api/guest/disk/detach")
    public ResultUtil<GuestModel> detachDisk(@RequestParam("guestId") int guestId,
                                             @RequestParam("volumeId") int volumeId) {
        GuestEntity guest = this.globalLockCall(() -> this.guestService.detachDisk(guestId, volumeId));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }

    @PostMapping("/api/guest/network/attach")

    public ResultUtil<AttachGuestNetworkModel> attachNetwork(@RequestParam("guestId") int guestId,
                                                             @RequestParam("networkId") int networkId,
                                                             @RequestParam("driver") String driveType) {
        AttachNicInfo attach = this.globalLockCall(() -> this.guestService.attachNetwork(guestId, networkId, driveType));
        AttachGuestNetworkModel model = new AttachGuestNetworkModel();
        model.setGuest(this.convertService.initGuestModel(attach.getGuest()));
        model.setNetwork(this.convertService.initGuestNetworkModel(attach.getNic()));
        return ResultUtil.success(model);

    }

    @PostMapping("/api/guest/network/detach")
    public ResultUtil<GuestModel> detachNetwork(@RequestParam("guestId") int guestId,
                                                @RequestParam("guestNetworkId") int guestNetworkId) {
        GuestEntity guest = this.globalLockCall(() -> this.guestService.detachNetwork(guestId, guestNetworkId));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }

    @PostMapping("/api/guest/modify")
    public ResultUtil<GuestModel> updateGuest(@RequestParam("guestId") int guestId,
                                              @RequestParam("category") int systemCategory,
                                              @RequestParam("bootstrapType") int bootstrapType,
                                              @RequestParam("description") String description,
                                              @RequestParam("schemeId") int schemeId,
                                              @RequestParam("groupId") int groupId) {
        GuestEntity guest = this.globalLockCall(() -> this.guestService.modifyGuest(guestId, systemCategory, bootstrapType, groupId, description, schemeId));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }


    @DeleteMapping("/api/guest/destroy")
    public ResultUtil<GuestModel> destroyGuest(@RequestParam("guestId") int guestId) {
        GuestEntity guest = this.globalLockCall(() -> this.guestService.destroyGuest(guestId));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }
    @PostMapping("/api/guest/host/bind")
    public ResultUtil<GuestModel> bindHost(@RequestParam("guestId") int guestId,
                                           @RequestParam("hostId") int hostId) {
        GuestEntity guest = this.globalLockCall(() -> this.guestService.bindGuestHost(guestId, hostId));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }
    @PostMapping("/api/guest/host/unbind")
    public ResultUtil<GuestModel> unbindHost(@RequestParam("guestId") int guestId) {
        GuestEntity guest = this.globalLockCall(() -> this.guestService.bindGuestHost(guestId, 0));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }

    @PutMapping("/api/guest/disk/bind/host")
    public ResultUtil<VolumeModel> bindHostDevice(@RequestParam("guestId") int guestId,
                                                  @RequestParam("deviceType") String deviceType,
                                                  @RequestParam(value = "diskFormat", required = false) String diskFormat,
                                                  @RequestParam("diskDriver") String diskDriver,
                                                  @RequestParam("devicePath") String devicePath,
                                                  @RequestParam("description") String description) {
        VolumeEntity volume = this.globalLockCall(() -> this.guestService.bindHostDevice(guestId, deviceType, description, devicePath, diskDriver, diskFormat));
        return ResultUtil.success(this.convertService.initVolumeModel(volume));
    }

}
