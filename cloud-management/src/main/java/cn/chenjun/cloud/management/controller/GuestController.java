package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.util.BeanConverter;
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
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/api/guest/graphics")
    public ResultUtil<GraphicsModel> getGuestGraphics(@RequestParam("guestId") int guestId) {
        GraphicsModel graphics = this.guestService.getGuestGraphics(guestId);

        return ResultUtil.success(graphics);
    }

    @GetMapping("/api/guest/network")
    public ResultUtil<List<GuestNetworkModel>> listGuestNetworks(@RequestParam("guestId") int guestId) {
        List<GuestNetworkEntity> nics = this.networkService.listGuestNetworks(guestId);
        BeanConverter.Converter<GuestNetworkEntity, GuestNetworkModel> converter = this.convertService::initGuestNetworkModel;
        List<GuestNetworkModel> models = BeanConverter.convert(nics, converter);
        return ResultUtil.success(models);
    }

    @GetMapping("/api/guest/disk")
    public ResultUtil<List<VolumeModel>> listGuestVolumes(@RequestParam("guestId") int guestId) {
        List<VolumeEntity> volumes = this.volumeService.listGuestVolumes(guestId);
        List<VolumeModel> models = volumes.stream().map(this.convertService::initVolumeModel).collect(Collectors.toList());
        return ResultUtil.success(models);
    }


    @PutMapping("/api/guest/create")
    public ResultUtil<GuestModel> createGuest(@RequestBody GuestCreateRequest request) {
        request.validate();
        GuestEntity guest = this.globalLockCall(() -> this.guestService.createGuest(request.getGroupId(), request.getDescription(), request.getCategory(), request.getBootstrapType(), request.getBootDeviceDriver(), request.getBindHostId(), request.getHostId(), request.getSchemeId(), request.getNetworkId(), request.getNetworkDeviceDriver(), request.getIsoTemplateId(), request.getDiskTemplateId(), request.getVolumeId(), request.getStorageId(), request.getDiskSize() * 1024 * 1024 * 1024, request.getHostname(), request.getPassword(), request.getSshId(), request.getArch(), request.getInitVendorData()));
        return ResultUtil.success(this.convertService.initGuestModel(guest));
    }


    @PostMapping("/api/guest/reinstall")
    public ResultUtil<GuestModel> reInstall(@RequestBody GuestReinstallRequest request) {
        request.validate();
        GuestEntity guest = this.globalLockCall(() -> this.guestService.reInstall(request.getGuestId(), request.getBootDeviceDriver(), request.getCategory(), request.getBootstrapType(), request.getIsoTemplateId(), request.getDiskTemplateId(), request.getVolumeId(), request.getStorageId(), request.getDiskSize() * 1024 * 1024 * 1024));
        return ResultUtil.success(this.convertService.initGuestModel(guest));
    }

    @PostMapping("/api/guest/start/batch")
    public ResultUtil<List<GuestModel>> batchStart(@RequestBody GuestBatchRequest request) {
        request.validate();
        List<GuestEntity> guests = this.globalLockCall(() -> this.guestService.batchStart(request.getGuestIds()));
        List<GuestModel> models = this.convertService.initGuestList(guests);
        return ResultUtil.success(models);
    }

    @PostMapping("/api/guest/shutdown/batch")
    public ResultUtil<List<GuestModel>> batchStop(@RequestBody GuestBatchRequest request) {
        request.validate();
        List<GuestEntity> guests = this.globalLockCall(() -> this.guestService.batchStop(request.getGuestIds(), request.isForce()));
        List<GuestModel> models = this.convertService.initGuestList(guests);
        return ResultUtil.success(models);
    }

    @PostMapping("/api/guest/start")
    public ResultUtil<GuestModel> start(@RequestBody GuestStartRequest request) {
        request.validate();
        GuestEntity guest = this.globalLockCall(() -> this.guestService.start(request.getGuestId(), request.getHostId()));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);

    }

    @PostMapping("/api/guest/reboot")
    public ResultUtil<GuestModel> reboot(@RequestBody GuestRebootRequest request) {
        request.validate();
        GuestEntity guest = this.globalLockCall(() -> this.guestService.reboot(request.getGuestId()));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }

    @PostMapping("/api/guest/migrate")
    public ResultUtil<GuestModel> migrate(@RequestBody GuestMigrateRequest request) {
        request.validate();
        GuestEntity guest = this.globalLockCall(() -> this.guestService.migrate(request.getGuestId(), request.getHostId()));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }


    @PostMapping("/api/guest/shutdown")
    public ResultUtil<GuestModel> shutdown(@RequestBody GuestShutdownRequest request) {
        request.validate();
        GuestEntity guest = this.globalLockCall(() -> this.guestService.shutdown(request.getGuestId(), request.isForce()));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);

    }

    @PostMapping("/api/guest/cd/attach")
    public ResultUtil<GuestModel> attachCdRoom(@RequestBody GuestCdAttachRequest request) {
        request.validate();
        GuestEntity guest = this.globalLockCall(() -> this.guestService.attachCdRoom(request.getGuestId(), request.getTemplateId()));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }

    @PostMapping("/api/guest/cd/detach")
    public ResultUtil<GuestModel> detachCdRoom(@RequestBody GuestCdDetachRequest request) {
        request.validate();
        GuestEntity guest = this.globalLockCall(() -> this.guestService.detachCdRoom(request.getGuestId()));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }

    @PostMapping("/api/guest/disk/modify")
    public ResultUtil<VolumeModel> modifyGuestDiskDeviceType(@RequestBody GuestDiskModifyRequest request) {
        request.validate();
        VolumeEntity volume = this.globalLockCall(() -> this.guestService.modifyGuestDiskDeviceType(request.getGuestId(), request.getDeviceId(), request.getDriver()));
        return ResultUtil.success(this.convertService.initVolumeModel(volume));
    }

    @PutMapping("/api/guest/disk/create")
    public ResultUtil<VolumeModel> createVolume(@RequestBody GuestDiskCreateRequest request) {
        request.validate();
        VolumeEntity volume = this.globalLockCall(() -> this.guestService.createDisk(request.getGuestId(), request.getDiskDriver(), request.getDescription(), request.getStorageId(), request.getVolumeSize() * 1024 * 1024 * 1024));
        return ResultUtil.success(this.convertService.initVolumeModel(volume));

    }

    @PostMapping("/api/guest/disk/attach")
    public ResultUtil<AttachGuestVolumeModel> attachDisk(@RequestBody GuestDiskAttachRequest request) {
        request.validate();
        AttachVolumeInfo attach = this.globalLockCall(() -> this.guestService.attachDisk(request.getGuestId(), request.getVolumeId(), request.getDiskDriver()));
        AttachGuestVolumeModel model = new AttachGuestVolumeModel();
        model.setGuest(this.convertService.initGuestModel(attach.getGuest()));
        model.setVolume(this.convertService.initVolumeModel(attach.getVolume()));
        return ResultUtil.success(model);
    }

    @PostMapping("/api/guest/disk/detach")
    public ResultUtil<GuestModel> detachDisk(@RequestBody GuestDiskDetachRequest request) {
        request.validate();
        GuestEntity guest = this.globalLockCall(() -> this.guestService.detachDisk(request.getGuestId(), request.getVolumeId()));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }

    @PostMapping("/api/guest/network/attach")
    public ResultUtil<AttachGuestNetworkModel> attachNetwork(@RequestBody GuestNetworkAttachRequest request) {
        request.validate();
        AttachNicInfo attach = this.globalLockCall(() -> this.guestService.attachNetwork(request.getGuestId(), request.getNetworkId(), request.getDriver()));
        AttachGuestNetworkModel model = new AttachGuestNetworkModel();
        model.setGuest(this.convertService.initGuestModel(attach.getGuest()));
        model.setNetwork(this.convertService.initGuestNetworkModel(attach.getNic()));
        return ResultUtil.success(model);

    }

    @PostMapping("/api/guest/network/detach")
    public ResultUtil<GuestModel> detachNetwork(@RequestBody GuestNetworkDetachRequest request) {
        request.validate();
        GuestEntity guest = this.globalLockCall(() -> this.guestService.detachNetwork(request.getGuestId(), request.getGuestNetworkId()));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }

    @PostMapping("/api/guest/modify")
    public ResultUtil<GuestModel> updateGuest(@RequestBody GuestModifyRequest request) {
        request.validate();
        GuestEntity guest = this.globalLockCall(() -> this.guestService.modifyGuest(request.getGuestId(), request.getCategory(), request.getBootstrapType(), request.getGroupId(), request.getDescription(), request.getSchemeId()));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }


    @DeleteMapping("/api/guest/destroy")
    public ResultUtil<GuestModel> destroyGuest(@RequestBody GuestDestroyRequest request) {
        request.validate();
        GuestEntity guest = this.globalLockCall(() -> this.guestService.destroyGuest(request.getGuestId()));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }

    @PostMapping("/api/guest/host/bind")
    public ResultUtil<GuestModel> bindHost(@RequestBody GuestBindHostRequest request) {
        request.validate();
        GuestEntity guest = this.globalLockCall(() -> this.guestService.bindGuestHost(request.getGuestId(), request.getHostId()));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }

    @PostMapping("/api/guest/host/unbind")
    public ResultUtil<GuestModel> unbindHost(@RequestBody GuestDestroyRequest request) {
        request.validate();
        GuestEntity guest = this.globalLockCall(() -> this.guestService.bindGuestHost(request.getGuestId(), 0));
        GuestModel model = this.convertService.initGuestModel(guest);
        return ResultUtil.success(model);
    }

    @PutMapping("/api/guest/disk/bind/host")
    public ResultUtil<VolumeModel> bindHostDevice(@RequestBody GuestBindHostDeviceRequest request) {
        request.validate();
        VolumeEntity volume = this.globalLockCall(() -> this.guestService.bindHostDevice(request.getGuestId(), request.getDeviceType(), request.getDescription(), request.getDevicePath(), request.getDiskDriver(), request.getDiskFormat()));
        return ResultUtil.success(this.convertService.initVolumeModel(volume));
    }

}
