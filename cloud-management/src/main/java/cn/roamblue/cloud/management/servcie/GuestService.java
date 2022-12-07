package cn.roamblue.cloud.management.servcie;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.*;
import cn.roamblue.cloud.management.data.mapper.*;
import cn.roamblue.cloud.management.model.GuestModel;
import cn.roamblue.cloud.management.operate.bean.*;
import cn.roamblue.cloud.management.task.OperateTask;
import cn.roamblue.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
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


    private StorageEntity allocateStorage(int storageId) {
        StorageEntity storage;
        if (storageId > 0) {
            storage = storageMapper.selectById(storageId);
            if (storage == null) {
                throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
            }
        } else {
            List<StorageEntity> storageList = storageMapper.selectList(new QueryWrapper<>());
            storageList = storageList.stream().filter(t -> Objects.equals(t.getStatus(), Constant.StorageStatus.READY)).collect(Collectors.toList());
            storage = storageList.stream().sorted((o1, o2) -> Long.compare(o2.getAvailable(), o1.getAvailable())).findFirst().orElse(null);
        }
        return storage;
    }

    private GuestNetworkEntity allocateNetwork(int networkId) {
        QueryWrapper<GuestNetworkEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("network_id", networkId);
        wrapper.eq("guest_id", 0);
        wrapper.last("limit 0,1");
        GuestNetworkEntity guestNetwork = guestNetworkMapper.selectOne(wrapper);
        if (guestNetwork == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_SPACE, "没有可用的网络资源");
        }
        return guestNetwork;
    }

    private HostEntity allocateHost(int hostId,int mustHostId,int cpu, long memory) {
        if(mustHostId>0){
            HostEntity host= this.hostMapper.selectById(mustHostId);
            if(host==null||!Objects.equals(host.getStatus(), Constant.HostStatus.ONLINE)){
                throw new CodeException(ErrorCode.SERVER_ERROR,"没有可用的主机");
            }
            if(host.getTotalMemory()-host.getAllocationMemory()>memory){
                throw new CodeException(ErrorCode.SERVER_ERROR,"主机当前可用内存不足");
            }
            if(host.getTotalCpu()-host.getAllocationCpu()>cpu){
                throw new CodeException(ErrorCode.SERVER_ERROR,"主机当前可用CPU不足");
            }
            return host;
        }else {
            List<HostEntity> list = this.hostMapper.selectList(new QueryWrapper<>());
            list = list.stream().filter(t -> Objects.equals(t.getStatus(), Constant.HostStatus.ONLINE))
                    .filter(t -> t.getTotalCpu() - t.getAllocationCpu() > cpu && t.getTotalMemory() - t.getAllocationMemory() > memory).collect(Collectors.toList());
            Collections.shuffle(list);
            HostEntity host = null;
            if (hostId > 0) {
                host = list.stream().filter(t -> Objects.equals(t.getHostId(), hostId)).findFirst().orElse(null);
            }
            if (host == null) {
                host = list.stream().findFirst().orElseThrow(() -> new CodeException(ErrorCode.HOST_NOT_SPACE, "没有可用的主机资源"));
            }
            return host;
        }
    }


    private GuestModel initGuestInfo(GuestEntity entity) {
        return GuestModel.builder().build();
    }

    public ResultUtil<List<GuestModel>> listGuests() {
        return ResultUtil.success(null);
    }

    public ResultUtil<GuestModel> getGuestInfo(int guestId) {
        return ResultUtil.success(null);
    }
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> createGuest(String description, String busType
            , int cpu, long memory, int networkId, String networkDeviceType,
                                              int cdRoom, int templateId,
                                              int storageId, String volumeType, long size) {


        String uid = UUID.randomUUID().toString().replace("-", "");
        HostEntity host = this.allocateHost(0,0,cpu, memory);
        GuestEntity guest = GuestEntity.builder()
                .name(uid)
                .description(description)
                .busType(busType)
                .cpu(cpu)
                .memory(memory)
                .cdRoom(cdRoom)
                .hostId(host.getHostId())
                .lastHostId(host.getHostId())
                .type(Constant.GuestType.USER)
                .status(Constant.GuestStatus.CREATING)
                .build();
        this.guestMapper.insert(guest);
        host.setAllocationCpu(host.getAllocationCpu()+cpu);
        host.setAllocationMemory(host.getAllocationCpu()+memory);
        this.hostMapper.updateById(host);
        GuestNetworkEntity guestNetwork = this.allocateNetwork(networkId);
        guestNetwork.setDeviceId(0);
        guestNetwork.setDriveType(networkDeviceType);
        guestNetwork.setGuestId(guest.getGuestId());
        this.guestNetworkMapper.insert(guestNetwork);
        StorageEntity storage = allocateStorage(storageId);
        VolumeEntity volume = VolumeEntity.builder()
                .capacity(size)
                .storageId(storage.getStorageId())
                .name(uid)
                .path(storage.getStorageId() + "/" + uid)
                .type(volumeType)
                .templateId(templateId)
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
                .volumeId(volume.getVolumeId())
                .taskId(uid)
                .build();
        this.operateTask.addTask(operateParam);
        return ResultUtil.success(this.initGuestInfo(guest));
    }
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> modifyGuest(int guestId, String description, String busType, int cpu, int memory) {
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

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> attachCdRoom(int guestId, int templateId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus() ) {
            case Constant.GuestStatus.STOP:
            case Constant.GuestStatus.RUNNING:
            throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
        }
        guest.setCdRoom(templateId);
        guest.setStatus(Constant.GuestStatus.ATTACH_CD_ROOM);
        this.guestMapper.updateById(guest);
        BaseOperateParam operateParam= ChangeGuestCdRoomOperate.builder().guestId(guestId).taskId(UUID.randomUUID().toString()).build();
        this.operateTask.addTask(operateParam);
        return ResultUtil.success(this.initGuestInfo(guest));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> detachCdRoom(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus() ) {
            case Constant.GuestStatus.STOP:
            case Constant.GuestStatus.RUNNING:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
        }
        guest.setCdRoom(0);
        guest.setStatus(Constant.GuestStatus.DETACH_CD_ROOM);
        this.guestMapper.updateById(guest);
        BaseOperateParam operateParam= ChangeGuestCdRoomOperate.builder().guestId(guestId).taskId(UUID.randomUUID().toString()).build();
        this.operateTask.addTask(operateParam);
        return ResultUtil.success(this.initGuestInfo(guest));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> attachDisk(int guestId, int volumeId) {
        GuestEntity guest=this.guestMapper.selectById(guestId);
        VolumeEntity volume=this.volumeMapper.selectById(volumeId);
        if(volume.getStatus()!= Constant.VolumeStatus.READY){
            throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘未就绪.");
        }
        GuestDiskEntity guestDisk=  this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq("volume_id",volume));
        if(guestDisk!=null){
            throw new CodeException(ErrorCode.SERVER_ERROR,"当前磁盘已经被挂载");
        }
        List<GuestDiskEntity> guestDiskList=this.guestDiskMapper.selectList(new QueryWrapper<GuestDiskEntity>().eq("guest_id",guestId));
        List<Integer> gustDiskDeviceIds=guestDiskList.stream().map(GuestDiskEntity::getDeviceId).collect(Collectors.toList());
        int deviceId=-1;
        for (int i = 0; i < Constant.MAX_DEVICE_ID; i++) {
            if(!gustDiskDeviceIds.contains(i)){
                deviceId=i;
                break;
            }
        }
        if(deviceId<0){
            throw new CodeException(ErrorCode.SERVER_ERROR,"当前挂载超过最大磁盘数量限制");
        }
        guestDisk=GuestDiskEntity.builder().volumeId(volumeId).deviceId(deviceId).build();
        this.guestDiskMapper.updateById(guestDisk);
        volume.setStatus(Constant.VolumeStatus.ATTACH_DISK);
        this.volumeMapper.updateById(volume);
        BaseOperateParam operateParam= ChangeGuestDiskOperate.builder()
                .guestDiskId(guestDisk.getGuestDiskId()).attach(true).volumeId(volumeId).guestId(guestId)
                .taskId(UUID.randomUUID().toString()).build();
        this.operateTask.addTask(operateParam);
        return ResultUtil.success(this.initGuestInfo(guest));
    }
    @Transactional(rollbackFor = Exception.class)

    public ResultUtil<GuestModel> detachDisk(int guestId, int guestDiskId) {
        GuestEntity guest=this.guestMapper.selectById(guestId);


        GuestDiskEntity guestDisk=  this.guestDiskMapper.selectById(guestDiskId);
        if(guestDisk==null){
            throw new CodeException(ErrorCode.SERVER_ERROR,"当前磁盘未挂载");
        }
        if(guestDisk.getGuestId()!=guestId){
            throw new CodeException(ErrorCode.SERVER_ERROR,"当前磁盘未挂载");
        }
        this.guestDiskMapper.deleteById(guestDiskId);
        BaseOperateParam operateParam= ChangeGuestDiskOperate.builder()
                .guestDiskId(guestDisk.getGuestDiskId()).attach(false).volumeId(guestDisk.getVolumeId()).guestId(guestId)
                .taskId(UUID.randomUUID().toString()).build();
        this.operateTask.addTask(operateParam);
        return ResultUtil.success(this.initGuestInfo(guest));
    }
    @Transactional(rollbackFor = Exception.class)

    public ResultUtil<GuestModel> attachNetwork(int guestId, int networkId,String driveType) {
        GuestEntity guest=this.guestMapper.selectById(guestId);
        GuestNetworkEntity guestNetwork=this.allocateNetwork(networkId);
        List<GuestNetworkEntity> guestNetworkList=this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("guest_id",guestId));
        List<Integer> guestNetworkDeviceIds=guestNetworkList.stream().map(GuestNetworkEntity::getDeviceId).collect(Collectors.toList());
        int deviceId=-1;
        for (int i = 0; i < Constant.MAX_DEVICE_ID; i++) {
            if(!guestNetworkDeviceIds.contains(i)){
                deviceId=i;
                break;
            }
        }
        if(deviceId<0){
            throw new CodeException(ErrorCode.SERVER_ERROR,"当前挂载超过最大网卡数量限制");
        }
        guestNetwork.setDeviceId(deviceId);
        guestNetwork.setDriveType(driveType);
        guestNetwork.setGuestId(guestId);
        this.guestNetworkMapper.updateById(guestNetwork);
        BaseOperateParam operateParam= ChangeGuestNetworkInterfaceOperate.builder()
                .guestNetworkId(guestNetwork.getGuestNetworkId()).attach(true).guestId(guestId)
                .taskId(UUID.randomUUID().toString()).build();
        this.operateTask.addTask(operateParam);
        return ResultUtil.success(this.initGuestInfo(guest));

    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> detachNetwork(int guestId, int guestNetworkId) {
        GuestEntity guest=this.guestMapper.selectById(guestId);
        GuestNetworkEntity guestNetwork=this.guestNetworkMapper.selectById(guestNetworkId);
        if(guestNetwork==null||guestNetwork.getGuestId()!=guestId){
            throw new CodeException(ErrorCode.SERVER_ERROR,"当前网卡未挂载");
        }
        guestNetwork.setDeviceId(0);
        guestNetwork.setGuestId(0);
        this.guestNetworkMapper.updateById(guestNetwork);
        BaseOperateParam operateParam= ChangeGuestNetworkInterfaceOperate.builder()
                .guestNetworkId(guestNetwork.getGuestNetworkId()).attach(false).guestId(guestId)
                .taskId(UUID.randomUUID().toString()).build();
        this.operateTask.addTask(operateParam);
        return ResultUtil.success(this.initGuestInfo(guest));
    }
}
