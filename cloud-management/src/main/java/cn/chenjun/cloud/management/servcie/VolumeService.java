package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.model.CloneModel;
import cn.chenjun.cloud.management.model.MigrateModel;
import cn.chenjun.cloud.management.model.SnapshotModel;
import cn.chenjun.cloud.management.model.VolumeModel;
import cn.chenjun.cloud.management.operate.bean.*;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class VolumeService extends AbstractService {

    @Autowired
    private AllocateService allocateService;
    @Autowired
    private RestTemplate restTemplate;

    private GuestEntity getVolumeGuest(int volumeId) {
        GuestDiskEntity guestDisk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq("volume_id", volumeId));
        if (guestDisk == null) {
            return null;
        }
        GuestEntity guest = guestMapper.selectById(guestDisk.getGuestId());
        return guest;
    }


    private VolumeEntity findAndUpdateVolumeStatus(int volumeId, int status) {
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


    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    public ResultUtil<List<VolumeModel>> listGuestVolumes(int guestId) {
        List<GuestDiskEntity> diskList = guestDiskMapper.selectList(new QueryWrapper<GuestDiskEntity>().eq("guest_id", guestId));
        diskList.sort(Comparator.comparingInt(GuestDiskEntity::getDeviceId));
        List<VolumeModel> models = diskList.stream().map(this::initVolume).sorted((o1, o2) -> {
            if (o1.getStatus() == o2.getStatus()) {
                return Integer.compare(o1.getVolumeId(), o2.getVolumeId());
            }
            if (o1.getStatus() == Constant.VolumeStatus.READY) {
                return -1;
            }
            if (o2.getStatus() == Constant.VolumeStatus.READY) {
                return 1;
            }
            return Integer.compare(o1.getStatus(), o2.getStatus());
        }).collect(Collectors.toList());
        return ResultUtil.success(models);

    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    public ResultUtil<List<VolumeModel>> listVolumes() {
        List<VolumeEntity> volumeList = this.volumeMapper.selectList(new QueryWrapper<>());
        List<VolumeModel> models = volumeList.stream().map(this::initVolume).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    public ResultUtil<List<VolumeModel>> listNoAttachVolumes() {
        List<Integer> volumeIds = this.guestDiskMapper.selectList(new QueryWrapper<>()).stream().map(GuestDiskEntity::getVolumeId).collect(Collectors.toList());
        List<VolumeEntity> volumeList = this.volumeMapper.selectList(new QueryWrapper<VolumeEntity>().notIn("volume_id", volumeIds));
        List<VolumeModel> models = volumeList.stream().map(this::initVolume).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    public ResultUtil<VolumeModel> getVolumeInfo(int volumeId) {
        VolumeEntity volume = this.volumeMapper.selectById(volumeId);
        if (volume == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在");
        }
        return ResultUtil.success(this.initVolume(volume));
    }


    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<VolumeModel> createVolume(String description, int storageId, int templateId, int snapshotVolumeId, String volumeType, long volumeSize) {
        if (StringUtils.isEmpty(description)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入磁盘备注");
        }
        if (StringUtils.isEmpty(volumeType)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择磁盘类型");
        }
        StorageEntity storage = this.allocateService.allocateStorage(storageId);
        String volumeName = UUID.randomUUID().toString();
        VolumeEntity volume = VolumeEntity.builder()
                .storageId(storage.getStorageId())
                .templateId(0)
                .description(description)
                .name(volumeName)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .backingPath("")
                .capacity(volumeSize)
                .allocation(0L)
                .status(Constant.VolumeStatus.CREATING)
                .createTime(new Date())
                .build();
        this.volumeMapper.insert(volume);
        BaseOperateParam operateParam = CreateVolumeOperate.builder().taskId(volumeName).title("创建磁盘[" + volume.getName() + "]").volumeId(volume.getVolumeId()).templateId(templateId).snapshotVolumeId(snapshotVolumeId).build();
        operateTask.addTask(operateParam);
        VolumeModel model = this.initVolume(volume);
        this.eventService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return ResultUtil.success(model);
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<CloneModel> cloneVolume(String description, int sourceVolumeId, int storageId, String volumeType) {
        if (StringUtils.isEmpty(volumeType)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择磁盘类型");
        }
        StorageEntity storage = this.allocateService.allocateStorage(storageId);
        String volumeName = UUID.randomUUID().toString();
        VolumeEntity volume = this.findAndUpdateVolumeStatus(sourceVolumeId, Constant.VolumeStatus.CLONE);
        GuestEntity guest = this.getVolumeGuest(sourceVolumeId);
        if (guest != null) {
            switch (guest.getStatus()) {
                case Constant.GuestStatus.STOP:
                case Constant.GuestStatus.ERROR:
                    break;
                default:
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘所在虚拟机正在运行,请关机后重试");
            }
        }
        volume.setStatus(Constant.VolumeStatus.CLONE);
        this.volumeMapper.updateById(volume);
        VolumeEntity cloneVolume = VolumeEntity.builder()
                .storageId(storage.getStorageId())
                .templateId(volume.getTemplateId())
                .name(volumeName)
                .description(description)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .backingPath("")
                .capacity(volume.getCapacity())
                .allocation(0L)
                .status(Constant.VolumeStatus.CREATING)
                .createTime(new Date())
                .build();
        this.volumeMapper.insert(cloneVolume);
        BaseOperateParam operateParam = CloneVolumeOperate.builder().taskId(volumeName)
                .sourceVolumeId(volume.getVolumeId())
                .targetVolumeId(cloneVolume.getVolumeId())
                .title("克隆磁盘[" + volume.getName() + "]")
                .build();
        operateTask.addTask(operateParam);
        VolumeModel source = this.initVolume(volume);
        VolumeModel clone = this.initVolume(cloneVolume);
        this.eventService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        this.eventService.publish(NotifyData.<Void>builder().id(cloneVolume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return ResultUtil.success(CloneModel.builder().source(source).clone(clone).build());
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<VolumeModel> resizeVolume(int volumeId, long size) {

        if (size <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入新增的磁盘大小");
        }
        VolumeEntity volume = this.findAndUpdateVolumeStatus(volumeId, Constant.VolumeStatus.RESIZE);
        volume.setCapacity(volume.getCapacity() + size);
        volume.setStatus(Constant.VolumeStatus.RESIZE);
        this.volumeMapper.updateById(volume);
        BaseOperateParam operateParam = ResizeVolumeOperate.builder().taskId(UUID.randomUUID().toString())
                .title("更改磁盘大小[" + volume.getName() + "]")
                .volumeId(volume.getVolumeId())
                .size(volume.getCapacity())
                .build();
        operateTask.addTask(operateParam);
        this.eventService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return ResultUtil.success(this.initVolume(volume));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<MigrateModel> migrateVolume(int sourceVolumeId, int storageId, String volumeType) {
        if (StringUtils.isEmpty(volumeType)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择迁移后磁盘类型");
        }
        StorageEntity storage = this.allocateService.allocateStorage(storageId);
        String volumeName = UUID.randomUUID().toString();
        VolumeEntity volume = this.findAndUpdateVolumeStatus(sourceVolumeId, Constant.VolumeStatus.MIGRATE);
        GuestEntity guest = this.getVolumeGuest(sourceVolumeId);
        if (guest != null) {
            switch (guest.getStatus()) {
                case Constant.GuestStatus.STOP:
                case Constant.GuestStatus.ERROR:
                    break;
                default:
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘所在虚拟机正在运行,请关机后重试");
            }
        }
        volume.setStatus(Constant.VolumeStatus.MIGRATE);
        this.volumeMapper.updateById(volume);
        VolumeEntity migrateVolume = VolumeEntity.builder()
                .description(volume.getDescription())
                .storageId(storage.getStorageId())
                .templateId(volume.getTemplateId())
                .name(volumeName)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .backingPath("")
                .capacity(volume.getCapacity())
                .allocation(0L)
                .status(Constant.VolumeStatus.CREATING)
                .createTime(new Date())
                .build();
        this.volumeMapper.insert(migrateVolume);
        BaseOperateParam operateParam = MigrateVolumeOperate.builder().taskId(volumeName)
                .title("迁移磁盘[" + volume.getName() + "]")
                .sourceVolumeId(volume.getVolumeId())
                .targetVolumeId(migrateVolume.getVolumeId())
                .build();
        operateTask.addTask(operateParam);
        VolumeModel source = this.initVolume(volume);
        VolumeModel migrate = this.initVolume(migrateVolume);
        this.eventService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        this.eventService.publish(NotifyData.<Void>builder().id(migrate.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return ResultUtil.success(MigrateModel.builder().source(source).migrate(migrate).build());
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<VolumeModel>> batchDestroyVolume(List<Integer> volumeIds) {
        List<VolumeModel> models = new ArrayList<>(volumeIds.size());
        for (Integer volumeId : volumeIds) {
            try {
                models.add(this.destroyVolume(volumeId).getData());
            } catch (Exception ignored) {

            }
        }
        return ResultUtil.success(models);
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
                    throw new CodeException(ErrorCode.GUEST_VOLUME_ATTACH_ERROR, "当前磁盘被系统挂载");
                }
                volume.setStatus(Constant.VolumeStatus.DESTROY);
                volumeMapper.updateById(volume);
                DestroyVolumeOperate operate = DestroyVolumeOperate.builder().taskId(UUID.randomUUID().toString()).title("销毁磁盘[" + volume.getName() + "]").volumeId(volumeId).build();
                operateTask.addTask(operate);
                VolumeModel source = this.initVolume(volume);
                this.eventService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
                return ResultUtil.success(source);
            default:
                throw new CodeException(ErrorCode.VOLUME_NOT_READY, "磁盘当前状态未就绪");
        }
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<SnapshotModel>> listSnapshot() {
        List<SnapshotVolumeEntity> snapshotVolumeList = this.snapshotVolumeMapper.selectList(new QueryWrapper<>());
        List<SnapshotModel> models = snapshotVolumeList.stream().map(this::initSnapshot).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SnapshotModel> getSnapshotInfo(int snapshotVolumeId) {
        SnapshotVolumeEntity snapshotVolume = this.snapshotVolumeMapper.selectById(snapshotVolumeId);
        if (snapshotVolume == null) {
            throw new CodeException(ErrorCode.SNAPSHOT_NOT_FOUND, "快照不存在");
        }
        this.eventService.publish(NotifyData.<Void>builder().id(snapshotVolume.getSnapshotVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_SNAPSHOT).build());
        return ResultUtil.success(this.initSnapshot(snapshotVolume));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SnapshotModel> createVolumeSnapshot(int volumeId, String snapshotName, String snapshotVolumeType) {
        if (StringUtils.isEmpty(snapshotName)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择快照类型");
        }
        if (StringUtils.isEmpty(snapshotVolumeType)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择快照磁盘类型");
        }
        StorageEntity storage = this.allocateService.allocateStorage(0);
        VolumeEntity volume = this.findAndUpdateVolumeStatus(volumeId, Constant.VolumeStatus.CREATE_SNAPSHOT);
        GuestEntity guest = this.getVolumeGuest(volumeId);
        if (guest != null) {
            switch (guest.getStatus()) {
                case Constant.GuestStatus.STOP:
                case Constant.GuestStatus.ERROR:
                    break;
                default:
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘所在虚拟机正在运行,请关机后重试");
            }
        }
        volume.setStatus(Constant.VolumeStatus.CREATE_SNAPSHOT);
        this.volumeMapper.updateById(volume);
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
        BaseOperateParam operateParam = CreateVolumeSnapshotOperate.builder().taskId(volumeName).title("创建磁盘快照[" + snapshotVolume.getName() + "]").sourceVolumeId(volume.getVolumeId()).snapshotVolumeId(snapshotVolume.getSnapshotVolumeId()).build();
        operateTask.addTask(operateParam);
        SnapshotModel model = this.initSnapshot(snapshotVolume);
        this.eventService.publish(NotifyData.<Void>builder().id(snapshotVolume.getSnapshotVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_SNAPSHOT).build());
        this.eventService.publish(NotifyData.<Void>builder().id(volumeId).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return ResultUtil.success(model);
    }


    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SnapshotModel> destroySnapshot(int snapshotVolumeId) {
        SnapshotVolumeEntity volume = this.snapshotVolumeMapper.selectById(snapshotVolumeId);
        if (volume == null) {
            return ResultUtil.error(ErrorCode.SNAPSHOT_NOT_FOUND, "快照不存在");
        }
        switch (volume.getStatus()) {
            case Constant.SnapshotStatus.ERROR:
            case Constant.SnapshotStatus.READY:
                volume.setStatus(Constant.SnapshotStatus.DESTROY);
                this.snapshotVolumeMapper.updateById(volume);
                BaseOperateParam operate = DestroySnapshotVolumeOperate.builder().taskId(UUID.randomUUID().toString()).title("删除磁盘快照[" + volume.getName() + "]").snapshotVolumeId(snapshotVolumeId).build();
                operateTask.addTask(operate);
                SnapshotModel source = this.initSnapshot(volume);
                this.eventService.publish(NotifyData.<Void>builder().id(snapshotVolumeId).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_SNAPSHOT).build());
                return ResultUtil.success(source);
            default:
                throw new CodeException(ErrorCode.VOLUME_NOT_READY, "快照当前状态未就绪");
        }
    }

}

