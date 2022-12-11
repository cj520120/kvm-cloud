package cn.roamblue.cloud.management.servcie;

import cn.hutool.core.convert.impl.BeanConverter;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.*;
import cn.roamblue.cloud.management.data.mapper.*;
import cn.roamblue.cloud.management.model.GuestModel;
import cn.roamblue.cloud.management.operate.bean.*;
import cn.roamblue.cloud.management.task.OperateTask;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class GuestService {

    @Autowired
    private GuestMapper guestMapper;
    @Autowired
    private VolumeMapper volumeMapper;
    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private GuestDiskMapper guestDiskMapper;
    @Autowired
    private NetworkMapper networkMapper;
    @Autowired
    private GuestNetworkMapper guestNetworkMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private TemplateMapper templateMapper;
    @Autowired
    private OperateTask operateTask;

    @Autowired
    private AllocateService allocateService;

    @Autowired
    private VolumeService volumeService;

    @Autowired
    private GuestVncMapper guestVncMapper;

    private GuestModel initGuestInfo(GuestEntity entity) {
        return new BeanConverter<>(GuestModel.class).convert(entity, null);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY,write = false)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<GuestModel>> listGuests() {
        List<GuestEntity> guestList = this.guestMapper.selectList(new QueryWrapper<>());
        List<GuestModel> models = guestList.stream().map(this::initGuestInfo).collect(Collectors.toList());
        return ResultUtil.success(models);
    }
    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY,write = false)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> getGuestInfo(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest == null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机不存在");
        }
        return ResultUtil.success(this.initGuestInfo(guest));
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> createGuest(String description, String busType
            ,int hostId, int cpu, long memory, int networkId, String networkDeviceType,
                                              int isoTemplateId, int diskTemplateId, int snapshotVolumeId, int volumeId,
                                              int storageId, String volumeType, long size) {


        String uid = UUID.randomUUID().toString().replace("-", "");
        HostEntity host = this.allocateService.allocateHost(hostId, hostId, cpu, memory);
        GuestEntity guest = GuestEntity.builder()
                .name(uid)
                .description(description)
                .busType(busType)
                .cpu(cpu)
                .memory(memory)
                .cdRoom(isoTemplateId)
                .hostId(host.getHostId())
                .lastHostId(0)
                .type(Constant.GuestType.USER)
                .status(Constant.GuestStatus.CREATING)
                .build();
        this.guestMapper.insert(guest);
        GuestNetworkEntity guestNetwork = this.allocateService.allocateNetwork(networkId);
        guestNetwork.setDeviceId(0);
        guestNetwork.setDriveType(networkDeviceType);
        guestNetwork.setGuestId(guest.getGuestId());
        this.guestNetworkMapper.insert(guestNetwork);
        StorageEntity storage = this.allocateService.allocateStorage(storageId);
        if (volumeId <= 0) {
            VolumeEntity volume = VolumeEntity.builder()
                    .capacity(size)
                    .storageId(storage.getStorageId())
                    .name(uid)
                    .path(storage.getMountPath() + "/" + uid)
                    .type(volumeType)
                    .templateId(diskTemplateId)
                    .allocation(0L)
                    .capacity(size)
                    .status(Constant.VolumeStatus.CREATING)
                    .build();
            this.volumeMapper.insert(volume);
            GuestDiskEntity guestDisk = GuestDiskEntity.builder()
                    .volumeId(volume.getVolumeId())
                    .guestId(guest.getGuestId())
                    .deviceId(0)
                    .build();
            this.guestDiskMapper.insert(guestDisk);
            BaseOperateParam operateParam = CreateGuestOperate.builder()
                    .guestId(guest.getGuestId())
                    .snapshotVolumeId(snapshotVolumeId)
                    .templateId(diskTemplateId)
                    .volumeId(volume.getVolumeId())
                    .hostId(hostId)
                    .taskId(uid)
                    .build();
            this.operateTask.addTask(operateParam);
        } else {
            GuestDiskEntity guestDisk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq("volume_id", volumeId));
            if (guestDisk != null) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘已经被挂载");
            }
            guestDisk = GuestDiskEntity.builder()
                    .volumeId(volumeId)
                    .guestId(guest.getGuestId())
                    .deviceId(0)
                    .build();
            this.guestDiskMapper.insert(guestDisk);
            guest.setStatus(Constant.GuestStatus.STARTING);
            this.guestMapper.updateById(guest);
            BaseOperateParam operateParam = StartGuestOperate.builder()
                    .guestId(guest.getGuestId())
                    .hostId(host.getHostId())
                    .taskId(uid)
                    .build();
            this.operateTask.addTask(operateParam);
        }
        return ResultUtil.success(this.initGuestInfo(guest));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> reInstall(int guestId,int isoTemplateId, int diskTemplateId, int snapshotVolumeId, int volumeId,
                                              int storageId, String volumeType, long size) {

        GuestEntity guest=this.guestMapper.selectById(guestId);
        if(guest.getStatus()!= Constant.GuestStatus.STOP){
            throw new CodeException(ErrorCode.SERVER_ERROR,"只能对关机状态对主机进行重装");
        }
        String uid = UUID.randomUUID().toString().replace("-", "");
        HostEntity host = this.allocateService.allocateHost(guest.getLastHostId(), 0, guest.getCpu(),guest.getMemory());
        guest.setCdRoom(isoTemplateId);
        host.setAllocationCpu(host.getAllocationCpu() + guest.getCpu());
        host.setAllocationMemory(host.getAllocationCpu() + guest.getMemory());
        this.hostMapper.updateById(host);
        this.guestDiskMapper.delete(new QueryWrapper<GuestDiskEntity>().eq("guest_id",guestId).eq("device_id",0));
        StorageEntity storage = this.allocateService.allocateStorage(storageId);
        if (volumeId <= 0) {
            VolumeEntity volume = VolumeEntity.builder()
                    .capacity(size)
                    .storageId(storage.getStorageId())
                    .name(uid)
                    .path(storage.getStorageId() + "/" + uid)
                    .type(volumeType)
                    .templateId(diskTemplateId)
                    .status(Constant.VolumeStatus.CREATING)
                    .build();
            this.volumeMapper.insert(volume);
            GuestDiskEntity guestDisk = GuestDiskEntity.builder()
                    .volumeId(volume.getVolumeId())
                    .guestId(guest.getGuestId())
                    .deviceId(0)
                    .build();
            this.guestDiskMapper.insert(guestDisk);
            guest.setStatus(Constant.GuestStatus.CREATING);
            this.guestMapper.updateById(guest);
            BaseOperateParam operateParam = CreateGuestOperate.builder()
                    .guestId(guest.getGuestId())
                    .snapshotVolumeId(snapshotVolumeId)
                    .templateId(diskTemplateId)
                    .volumeId(volume.getVolumeId())
                    .taskId(uid)
                    .build();
            this.operateTask.addTask(operateParam);
        } else {
            GuestDiskEntity guestDisk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq("volume_id", volumeId));
            if (guestDisk != null) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘已经被挂载");
            }
            guestDisk = GuestDiskEntity.builder()
                    .volumeId(volumeId)
                    .guestId(guest.getGuestId())
                    .deviceId(0)
                    .build();
            this.guestDiskMapper.insert(guestDisk);
            guest.setStatus(Constant.GuestStatus.STARTING);
            this.guestMapper.updateById(guest);
            BaseOperateParam operateParam = StartGuestOperate.builder()
                    .guestId(guest.getGuestId())
                    .hostId(host.getHostId())
                    .taskId(uid)
                    .build();
            this.operateTask.addTask(operateParam);
        }
        return ResultUtil.success(this.initGuestInfo(guest));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> start(int guestId, int hostId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest.getStatus() == Constant.GuestStatus.STOP) {
            HostEntity host = this.allocateService.allocateHost(guest.getLastHostId(), hostId, guest.getCpu(), guest.getMemory());
            guest.setHostId(host.getHostId());
            guest.setLastHostId(host.getHostId());
            guest.setStatus(Constant.GuestStatus.STARTING);
            this.guestMapper.updateById(guest);
            BaseOperateParam operateParam = StartGuestOperate.builder().guestId(guestId).taskId(UUID.randomUUID().toString()).build();
            this.operateTask.addTask(operateParam);
            return ResultUtil.success(this.initGuestInfo(guest));
        }
        throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");

    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> reboot(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest.getStatus() == Constant.GuestStatus.RUNNING) {
            guest.setStatus(Constant.GuestStatus.REBOOT);
            this.guestMapper.updateById(guest);
            BaseOperateParam operateParam = RebootGuestOperate.builder().guestId(guestId).taskId(UUID.randomUUID().toString()).build();
            this.operateTask.addTask(operateParam);
            return ResultUtil.success(this.initGuestInfo(guest));
        }
        throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> shutdown(int guestId, boolean force) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus()) {
            case Constant.GuestStatus.RUNNING:
            case Constant.GuestStatus.STOPPING:
                guest.setStatus(Constant.GuestStatus.STOPPING);
                this.guestMapper.updateById(guest);
                BaseOperateParam operateParam = StopGuestOperate.builder().guestId(guestId).force(force).taskId(UUID.randomUUID().toString()).build();
                this.operateTask.addTask(operateParam);
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
        }

    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> modifyGuest(int guestId, String description, String busType, int cpu, long memory) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest.getStatus() != Constant.GuestStatus.STOP) {
            throw new CodeException(ErrorCode.VM_NOT_STOP, "请首先停止系统");
        }
        guest.setCpu(cpu);
        guest.setMemory(memory);
        guest.setBusType(busType);
        guest.setDescription(description);
        this.guestMapper.updateById(guest);
        return ResultUtil.success(this.initGuestInfo(guest));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> attachCdRoom(int guestId, int templateId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus() ) {
            case Constant.GuestStatus.STOP:
            case Constant.GuestStatus.RUNNING:
                guest.setCdRoom(templateId);
                this.guestMapper.updateById(guest);
                BaseOperateParam operateParam= ChangeGuestCdRoomOperate.builder().guestId(guestId).taskId(UUID.randomUUID().toString()).build();
                this.operateTask.addTask(operateParam);
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
        }
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> detachCdRoom(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus() ) {
            case Constant.GuestStatus.STOP:
            case Constant.GuestStatus.RUNNING:
                guest.setCdRoom(0);
                this.guestMapper.updateById(guest);
                BaseOperateParam operateParam= ChangeGuestCdRoomOperate.builder().guestId(guestId).taskId(UUID.randomUUID().toString()).build();
                this.operateTask.addTask(operateParam);
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
        }
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> attachDisk(int guestId, int volumeId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
            case Constant.GuestStatus.RUNNING:
                VolumeEntity volume = this.volumeMapper.selectById(volumeId);
                if (volume.getStatus() != Constant.VolumeStatus.READY) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘未就绪.");
                }
                GuestDiskEntity guestDisk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq("volume_id", volume));
                if (guestDisk != null) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘已经被挂载");
                }
                List<GuestDiskEntity> guestDiskList = this.guestDiskMapper.selectList(new QueryWrapper<GuestDiskEntity>().eq("guest_id", guestId));
                List<Integer> gustDiskDeviceIds = guestDiskList.stream().map(GuestDiskEntity::getDeviceId).collect(Collectors.toList());
                int deviceId = -1;
                for (int i = 0; i < Constant.MAX_DEVICE_ID; i++) {
                    if (!gustDiskDeviceIds.contains(i)) {
                        deviceId = i;
                        break;
                    }
                }
                if (deviceId < 0) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前挂载超过最大磁盘数量限制");
                }
                guestDisk = GuestDiskEntity.builder().volumeId(volumeId).deviceId(deviceId).build();
                this.guestDiskMapper.updateById(guestDisk);
                volume.setStatus(Constant.VolumeStatus.ATTACH_DISK);
                this.volumeMapper.updateById(volume);
                BaseOperateParam operateParam = ChangeGuestDiskOperate.builder()
                        .guestDiskId(guestDisk.getGuestDiskId()).attach(true).volumeId(volumeId).guestId(guestId)
                        .taskId(UUID.randomUUID().toString()).build();
                this.operateTask.addTask(operateParam);
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态未就绪.");
        }
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> detachDisk(int guestId, int guestDiskId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);

        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
            case Constant.GuestStatus.RUNNING:
                GuestDiskEntity guestDisk = this.guestDiskMapper.selectById(guestDiskId);
                if (guestDisk == null) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘未挂载");
                }
                if (guestDisk.getGuestId() != guestId) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘未挂载");
                }
                this.guestDiskMapper.deleteById(guestDiskId);
                BaseOperateParam operateParam = ChangeGuestDiskOperate.builder()
                        .guestDiskId(guestDisk.getGuestDiskId()).attach(false).volumeId(guestDisk.getVolumeId()).guestId(guestId)
                        .taskId(UUID.randomUUID().toString()).build();
                this.operateTask.addTask(operateParam);
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态未就绪.");
        }
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> attachNetwork(int guestId, int networkId,String driveType) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
            case Constant.GuestStatus.RUNNING:
                GuestNetworkEntity guestNetwork = this.allocateService.allocateNetwork(networkId);
                List<GuestNetworkEntity> guestNetworkList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", guestId));
                List<Integer> guestNetworkDeviceIds = guestNetworkList.stream().map(GuestNetworkEntity::getDeviceId).collect(Collectors.toList());
                int deviceId = -1;
                for (int i = 0; i < Constant.MAX_DEVICE_ID; i++) {
                    if (!guestNetworkDeviceIds.contains(i)) {
                        deviceId = i;
                        break;
                    }
                }
                if (deviceId < 0) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前挂载超过最大网卡数量限制");
                }
                guestNetwork.setDeviceId(deviceId);
                guestNetwork.setDriveType(driveType);
                guestNetwork.setGuestId(guestId);
                this.guestNetworkMapper.updateById(guestNetwork);
                BaseOperateParam operateParam = ChangeGuestNetworkInterfaceOperate.builder()
                        .guestNetworkId(guestNetwork.getGuestNetworkId()).attach(true).guestId(guestId)
                        .taskId(UUID.randomUUID().toString()).build();
                this.operateTask.addTask(operateParam);
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态未就绪.");
        }

    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> detachNetwork(int guestId, int guestNetworkId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
            case Constant.GuestStatus.RUNNING:
                GuestNetworkEntity guestNetwork = this.guestNetworkMapper.selectById(guestNetworkId);
                if (guestNetwork == null || guestNetwork.getGuestId() != guestId) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前网卡未挂载");
                }
                guestNetwork.setDeviceId(0);
                guestNetwork.setGuestId(0);
                this.guestNetworkMapper.updateById(guestNetwork);
                BaseOperateParam operateParam = ChangeGuestNetworkInterfaceOperate.builder()
                        .guestNetworkId(guestNetwork.getGuestNetworkId()).attach(false).guestId(guestId)
                        .taskId(UUID.randomUUID().toString()).build();
                this.operateTask.addTask(operateParam);
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态未就绪.");
        }
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<Void> destroyGuest(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus()) {
            case Constant.GuestStatus.ERROR:
            case Constant.GuestStatus.STOP:
                List<GuestNetworkEntity> guestNetworkList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", guestId));
                for (GuestNetworkEntity guestNetwork : guestNetworkList) {
                    guestNetwork.setGuestId(0);
                    guestNetwork.setDeviceId(0);
                    guestNetwork.setDriveType("");
                    this.guestNetworkMapper.updateById(guestNetwork);
                }
                List<GuestDiskEntity> guestDiskList = this.guestDiskMapper.selectList(new QueryWrapper<GuestDiskEntity>().eq("guest_id", guestId));
                for (GuestDiskEntity guestDisk : guestDiskList) {
                    this.guestDiskMapper.deleteById(guestDisk.getGuestDiskId());
                }
                List<VolumeEntity> guestVolumeList = this.volumeMapper.selectBatchIds(guestDiskList.stream().map(GuestDiskEntity::getVolumeId).collect(Collectors.toList()));
                for (VolumeEntity volume : guestVolumeList) {
                    this.volumeService.destroyVolume(volume.getVolumeId());
                }
                this.guestVncMapper.delete(new QueryWrapper<GuestVncEntity>().eq("guest_id", guest.getGuestId()));
                return ResultUtil.success();
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机不是关机状态");
        }
    }
}
