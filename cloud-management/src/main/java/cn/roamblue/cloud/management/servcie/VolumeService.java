package cn.roamblue.cloud.management.servcie;

import cn.hutool.core.convert.impl.BeanConverter;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.*;
import cn.roamblue.cloud.management.data.mapper.*;
import cn.roamblue.cloud.management.model.*;
import cn.roamblue.cloud.management.operate.bean.*;
import cn.roamblue.cloud.management.task.OperateTask;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VolumeService {
    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private VolumeMapper volumeMapper;
    @Autowired
    private TemplateMapper templateMapper;
    @Autowired
    private OperateTask operateTask;
    @Autowired
    private TemplateVolumeMapper templateVolumeMapper;
    @Autowired
    private SnapshotVolumeMapper snapshotVolumeMapper;
    @Autowired
    private GuestDiskMapper guestDiskMapper;
    @Autowired
    private AllocateService allocateService;

    @Autowired
    private GuestMapper guestMapper;

    private GuestEntity getVolumeGuest(int volumeId) {
        GuestDiskEntity guestDisk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq("volume_id", volumeId));
        if (guestDisk == null) {
            return null;
        }
        GuestEntity guest = guestMapper.selectById(guestDisk.getGuestId());
        return guest;
    }


    private VolumeEntity findAndUpdateVolumeStatus(int volumeId,int status){
        VolumeEntity volume = this.volumeMapper.selectById(volumeId);
        if (volume == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在");
        }
        if (!Objects.equals(volume.getStatus(), Constant.VolumeStatus.READY)) {
            throw new CodeException(ErrorCode.VOLUME_NOT_READY, "磁盘当前状态未就绪");
        }
        volume.setStatus(status);
        this.volumeMapper.updateById(volume);
        return volume;
    }


    private VolumeModel initVolume(VolumeEntity volume) {
        VolumeModel model = new BeanConverter<>(VolumeModel.class).convert(volume, null);
        GuestDiskEntity disk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq("volume_id", volume.getVolumeId()));
        if (disk != null) {
            model.setAttach(VolumeAttachModel.builder().guestId(disk.getGuestId()).deviceId(disk.getDeviceId()).build());
        }
        return model;
    }
    private SnapshotModel initSnapshot(SnapshotVolumeEntity volume) {
        SnapshotModel model = new BeanConverter<>(SnapshotModel.class).convert(volume, null);

        return model;
    }
    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY,write = false)
    public ResultUtil<List<VolumeModel>> listVolumes() {
        List<VolumeEntity> volumeList = this.volumeMapper.selectList(new QueryWrapper<>());
        List<VolumeModel> models = volumeList.stream().map(this::initVolume).collect(Collectors.toList());
        return ResultUtil.success(models);
    }
    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY,write = false)
    public ResultUtil<VolumeModel> getVolumeInfo(int volumeId) {
        VolumeEntity volume = this.volumeMapper.selectById(volumeId);
        if (volume == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在");
        }
        return ResultUtil.success(this.initVolume(volume));
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<VolumeModel> createVolume(int storageId, int templateId,int snapshotVolumeId, String volumeType, long volumeSize) {
        StorageEntity storage = this.allocateService.allocateStorage(storageId);
        String volumeName = UUID.randomUUID().toString();
        VolumeEntity volume = VolumeEntity.builder()
                .storageId(storage.getStorageId())
                .templateId(0)
                .name(volumeName)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .capacity(volumeSize)
                .allocation(0L)
                .status(Constant.VolumeStatus.CREATING)
                .createTime(new Date())
                .build();
        this.volumeMapper.insert(volume);
        BaseOperateParam operateParam = CreateVolumeOperate.builder().taskId(volumeName).volumeId(volume.getVolumeId()).templateId(templateId).snapshotVolumeId(snapshotVolumeId).build();
        operateTask.addTask(operateParam);
        VolumeModel model =this.initVolume(volume);
        return ResultUtil.success(model);
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<CloneModel> cloneVolume(int sourceVolumeId, int storageId, String volumeType) {
        StorageEntity storage = this.allocateService.allocateStorage(storageId);
        String volumeName = UUID.randomUUID().toString();
        VolumeEntity volume = this.findAndUpdateVolumeStatus(sourceVolumeId, Constant.VolumeStatus.CLONE);
        GuestEntity guest = this.getVolumeGuest(sourceVolumeId);
        if (guest != null) {
            switch (guest.getStatus()) {
                case Constant.GuestStatus.STOP:
                case Constant.GuestStatus.ERROR:
                case Constant.GuestStatus.DESTROY:
                    break;
                default:
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘所在虚拟机正在运行,请关机后重试");
            }
        }
        VolumeEntity cloneVolume = VolumeEntity.builder()
                .storageId(storage.getStorageId())
                .templateId(volume.getTemplateId())
                .name(volumeName)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .capacity(volume.getCapacity())
                .allocation(0L)
                .status(Constant.VolumeStatus.CREATING)
                .createTime(new Date())
                .build();
        this.volumeMapper.insert(cloneVolume);
        BaseOperateParam operateParam = CloneVolumeOperate.builder().taskId(volumeName)
                .sourceVolumeId(volume.getVolumeId())
                .targetVolumeId(cloneVolume.getVolumeId())
                .build();
        operateTask.addTask(operateParam);
        VolumeModel source = this.initVolume(volume);
        VolumeModel clone = this.initVolume(cloneVolume);
        return ResultUtil.success(CloneModel.builder().source(source).clone(clone).build());
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<VolumeModel> resizeVolume(int volumeId,long size) {
        VolumeEntity volume = this.findAndUpdateVolumeStatus(volumeId, Constant.VolumeStatus.RESIZE);

        BaseOperateParam operateParam = ResizeVolumeOperate.builder().taskId(UUID.randomUUID().toString())
                .volumeId(volume.getVolumeId())
                .size(size)
                .build();
        operateTask.addTask(operateParam);
        return ResultUtil.success(this.initVolume(volume));
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<MigrateModel> migrateVolume(int sourceVolumeId, int storageId, String volumeType) {
        StorageEntity storage = this.allocateService.allocateStorage(storageId);
        String volumeName = UUID.randomUUID().toString();
        VolumeEntity volume = this.findAndUpdateVolumeStatus(sourceVolumeId, Constant.VolumeStatus.MIGRATE);
        GuestEntity guest = this.getVolumeGuest(sourceVolumeId);
        if (guest != null) {
            switch (guest.getStatus()) {
                case Constant.GuestStatus.STOP:
                case Constant.GuestStatus.ERROR:
                case Constant.GuestStatus.DESTROY:
                    break;
                default:
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘所在虚拟机正在运行,请关机后重试");
            }
        }
        VolumeEntity migrateVolume = VolumeEntity.builder()
                .storageId(storage.getStorageId())
                .templateId(volume.getTemplateId())
                .name(volumeName)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .capacity(volume.getCapacity())
                .allocation(0L)
                .status(Constant.VolumeStatus.CREATING)
                .createTime(new Date())
                .build();
        this.volumeMapper.insert(migrateVolume);
        BaseOperateParam operateParam = MigrateVolumeOperate.builder().taskId(volumeName)
                .sourceVolumeId(volume.getVolumeId())
                .targetVolumeId(migrateVolume.getVolumeId())
                .build();
        operateTask.addTask(operateParam);
        VolumeModel source = this.initVolume(volume);
        VolumeModel migrate =this.initVolume(migrateVolume);
        return ResultUtil.success(MigrateModel.builder().source(source).migrate(migrate).build());
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<VolumeModel> destroyVolume(int volumeId) {
        VolumeEntity volume = this.volumeMapper.selectById(volumeId);
        if (volume == null) {
            return ResultUtil.error(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在");
        }
        GuestEntity guest = this.getVolumeGuest(volumeId);
        if (guest != null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘正在被虚拟机挂载，请卸载后重试");
        }
        switch (volume.getStatus()) {
            case Constant.VolumeStatus.ERROR:
            case Constant.VolumeStatus.READY:
                if (guestDiskMapper.selectCount(new QueryWrapper<GuestDiskEntity>().eq("volume_id", volumeId)) > 0) {
                    throw new CodeException(ErrorCode.VOLUME_ATTACH_ERROR, "当前磁盘被系统挂载");
                }
                volume.setStatus(Constant.VolumeStatus.DESTROY);
                volumeMapper.updateById(volume);
                DestroyVolumeOperate operate = DestroyVolumeOperate.builder().taskId(UUID.randomUUID().toString()).volumeId(volumeId).build();
                operateTask.addTask(operate);
                VolumeModel source = this.initVolume(volume);
                return ResultUtil.success(source);
            default:
                throw new CodeException(ErrorCode.VOLUME_NOT_READY, "磁盘当前状态未就绪");
        }
    }
    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY,write = false)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<SnapshotModel>> listSnapshot(){
       List<SnapshotVolumeEntity> snapshotVolumeList =this.snapshotVolumeMapper.selectList(new QueryWrapper<>());
        List<SnapshotModel> models=snapshotVolumeList.stream().map(this::initSnapshot).collect(Collectors.toList());
        return ResultUtil.success(models);
    }
    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY,write = false)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SnapshotModel> getSnapshotInfo(int snapshotVolumeId){
        SnapshotVolumeEntity snapshotVolume=this.snapshotVolumeMapper.selectById(snapshotVolumeId);
        if(snapshotVolume==null){
            throw new CodeException(ErrorCode.SERVER_ERROR,"快照不存在");
        }
        return ResultUtil.success(this.initSnapshot(snapshotVolume));
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SnapshotModel> createVolumeSnapshot(int volumeId,String snapshotName,String snapshotVolumeType) {
        StorageEntity storage = this.allocateService.allocateStorage(0);
        VolumeEntity volume = this.findAndUpdateVolumeStatus(volumeId, Constant.VolumeStatus.CREATE_SNAPSHOT);
        GuestEntity guest = this.getVolumeGuest(volumeId);
        if (guest != null) {
            switch (guest.getStatus()) {
                case Constant.GuestStatus.STOP:
                case Constant.GuestStatus.ERROR:
                case Constant.GuestStatus.DESTROY:
                    break;
                default:
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘所在虚拟机正在运行,请关机后重试");
            }
        }

        String volumeName = UUID.randomUUID().toString();
        SnapshotVolumeEntity snapshotVolume = SnapshotVolumeEntity.builder()
                .name(snapshotName)
                .storageId(storage.getStorageId())
                .volumeName(volumeName)
                .volumePath(storage.getMountPath() + "/" + volumeName)
                .type(snapshotVolumeType)
                .capacity(0L)
                .allocation(0L)
                .status(Constant.SnapshotStatus.CREATING)
                .createTime(new Date())
                .build();
        this.snapshotVolumeMapper.insert(snapshotVolume);
        BaseOperateParam operateParam = CreateVolumeSnapshotOperate.builder().taskId(volumeName).sourceVolumeId(volume.getVolumeId()).snapshotVolumeId(snapshotVolume.getSnapshotVolumeId()).build();
        operateTask.addTask(operateParam);
        SnapshotModel model =this.initSnapshot(snapshotVolume);
        return ResultUtil.success(model);
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SnapshotModel> destroySnapshot(int snapshotVolumeId) {
        SnapshotVolumeEntity volume = this.snapshotVolumeMapper.selectById(snapshotVolumeId);
        if (volume == null) {
            return ResultUtil.error(ErrorCode.VOLUME_NOT_FOUND, "快照不存在");
        }
        switch (volume.getStatus()) {
            case Constant.SnapshotStatus.ERROR:
            case Constant.SnapshotStatus.READY:
                volume.setStatus(Constant.SnapshotStatus.DESTROY);
                this.snapshotVolumeMapper.updateById(volume);
                BaseOperateParam operate = DestroySnapshotVolumeOperate.builder().taskId(UUID.randomUUID().toString()).snapshotVolumeId(snapshotVolumeId).build();
                operateTask.addTask(operate);
                SnapshotModel source = this.initSnapshot(volume);
                return ResultUtil.success(source);
            default:
                throw new CodeException(ErrorCode.VOLUME_NOT_READY, "快照当前状态未就绪");
        }
    }

}

