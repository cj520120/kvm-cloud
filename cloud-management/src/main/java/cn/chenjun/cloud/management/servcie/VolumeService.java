package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.operate.bean.*;
import cn.chenjun.cloud.management.servcie.bean.CloneInfo;
import cn.chenjun.cloud.management.servcie.bean.MigrateVolumeInfo;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.DiskSerialUtil;
import cn.chenjun.cloud.management.util.NameUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author chenjun
 */
@Service
public class VolumeService extends AbstractService {

    @Autowired
    private AllocateService allocateService;
    @Autowired
    private TemplateService templateService;


    private VolumeEntity findAndUpdateVolumeStatus(int volumeId, int status) {
        VolumeEntity volume = this.volumeDao.findById(volumeId);
        if (volume == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在");
        }
        if (!Objects.equals(volume.getStatus(), cn.chenjun.cloud.common.util.Constant.VolumeStatus.READY)) {
            throw new CodeException(ErrorCode.VOLUME_NOT_READY, "磁盘当前状态未就绪");
        }
        volume.setStatus(status);
        this.volumeDao.update(volume);
        return volume;
    }


    public List<VolumeEntity> listGuestVolumes(int guestId) {
        List<VolumeEntity> diskList = this.volumeDao.listByGuestId(guestId);
        Collections.sort(diskList, (o1, o2) -> {
            if (o1.getStatus() == o2.getStatus()) {
                return Integer.compare(o1.getVolumeId(), o2.getVolumeId());
            }
            if (o1.getStatus() == cn.chenjun.cloud.common.util.Constant.VolumeStatus.READY) {
                return -1;
            }
            if (o2.getStatus() == cn.chenjun.cloud.common.util.Constant.VolumeStatus.READY) {
                return 1;
            }
            return Integer.compare(o1.getStatus(), o2.getStatus());
        });

        return diskList;
    }

    public Page<VolumeEntity> search(Integer storageId, Integer status, Integer templateId, String volumeType, String keyword, int no, int size) {
        Page<VolumeEntity> page = this.volumeDao.search(storageId, status, templateId, volumeType, keyword, no, size);
        return page;

    }

    public List<VolumeEntity> listVolumes() {
        List<VolumeEntity> volumeList = this.volumeDao.listAll();
        return volumeList;
    }

    public List<VolumeEntity> listNoAttachVolumes(int guestId) {
        GuestEntity guest = this.guestDao.findById(guestId);
        List<VolumeEntity> volumeList = this.volumeDao.listByGuestId(0);
        int allowHostId = guest == null ? 0 : guest.getBindHostId();
        List<VolumeEntity> models = new ArrayList<>();
        for (VolumeEntity volume : volumeList) {
            if (!Objects.equals(volume.getStatus(), cn.chenjun.cloud.common.util.Constant.VolumeStatus.READY)) {
                continue;
            }
            if (allowHostId == 0 || volume.getHostId() == 0 || volume.getHostId() == allowHostId) {
                models.add(volume);
            }
        }
        return models;
    }


    @Transactional(rollbackFor = Exception.class)
    public VolumeEntity createVolume(String description, int storageId, int templateId, long volumeSize) {
        if (StringUtils.isEmpty(description)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入磁盘备注");
        }
        StorageEntity storage = this.allocateService.allocateStorage(cn.chenjun.cloud.common.util.Constant.StorageCategory.VOLUME, storageId);
        String volumeType = getVolumeType(storage);
        String volumeName = NameUtil.generateVolumeName();
        VolumeEntity volume = VolumeEntity.builder()
                .storageId(storage.getStorageId())
                .templateId(0)
                .hostId(storage.getHostId())
                .description(description)
                .name(volumeName)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .capacity(volumeSize)
                .allocation(0L)
                .status(cn.chenjun.cloud.common.util.Constant.VolumeStatus.CREATING)
                .deviceDriver("")
                .guestId(0)
                .deviceId(0)
                .createTime(new Date())
                .device(Constant.DeviceType.DISK)
                .serial(DiskSerialUtil.generateDiskSerial())
                .build();
        this.volumeDao.insert(volume);
        BaseOperateParam operateParam = CreateVolumeOperate.builder().id(UUID.randomUUID().toString()).title("创建磁盘[" + volume.getName() + "]").volumeId(volume.getVolumeId()).templateId(templateId).build();
        operateTask.addTask(operateParam);

        this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return volume;
    }

    @Transactional(rollbackFor = Exception.class)
    public CloneInfo cloneVolume(String description, int sourceVolumeId, int storageId) {

        StorageEntity storage = this.allocateService.allocateStorage(cn.chenjun.cloud.common.util.Constant.StorageCategory.VOLUME, storageId);
        String volumeType = getVolumeType(storage);
        String volumeName = NameUtil.generateVolumeName();
        VolumeEntity volume = this.findAndUpdateVolumeStatus(sourceVolumeId, cn.chenjun.cloud.common.util.Constant.VolumeStatus.CLONE);
        if (volume.getHostId() > 0 && storage.getHostId() > 0 && !Objects.equals(volume.getHostId(), storage.getHostId())) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "跨宿主机本地磁盘无法完成克隆，如需进行操作，请先迁移到共享存储，然后再进行克隆");
        }
        GuestEntity guest = this.getVolumeGuest(sourceVolumeId);
        if (guest != null) {
            switch (guest.getStatus()) {
                case cn.chenjun.cloud.common.util.Constant.GuestStatus.STOP:
                case cn.chenjun.cloud.common.util.Constant.GuestStatus.ERROR:
                    break;
                default:
                    throw new CodeException(ErrorCode.GUEST_NOT_STOP, "当前磁盘所在虚拟机正在运行,请关机后重试");
            }
        }
        volume.setStatus(cn.chenjun.cloud.common.util.Constant.VolumeStatus.CLONE);
        this.volumeDao.update(volume);
        VolumeEntity cloneVolume = VolumeEntity.builder()
                .storageId(storage.getStorageId())
                .hostId(storage.getHostId())
                .templateId(volume.getTemplateId())
                .name(volumeName)
                .description(description)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .capacity(volume.getCapacity())
                .allocation(0L)
                .status(cn.chenjun.cloud.common.util.Constant.VolumeStatus.CREATING)
                .device(Constant.DeviceType.DISK)
                .serial(DiskSerialUtil.generateDiskSerial())
                .createTime(new Date())
                .build();
        this.volumeDao.insert(cloneVolume);
        BaseOperateParam operateParam = CloneVolumeOperate.builder().id(UUID.randomUUID().toString())
                .sourceVolumeId(volume.getVolumeId())
                .targetVolumeId(cloneVolume.getVolumeId())
                .title("克隆磁盘[" + volume.getName() + "]")
                .build();
        operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        this.notifyService.publish(NotifyData.<Void>builder().id(cloneVolume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return CloneInfo.builder().source(volume).clone(cloneVolume).build();
    }

    @Transactional(rollbackFor = Exception.class)
    public VolumeEntity resizeVolume(int volumeId, long size) {
        if (size <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入新增的磁盘大小");
        }
        VolumeEntity volume = this.findAndUpdateVolumeStatus(volumeId, cn.chenjun.cloud.common.util.Constant.VolumeStatus.RESIZE);
        volume.setCapacity(volume.getCapacity() + size);
        volume.setStatus(cn.chenjun.cloud.common.util.Constant.VolumeStatus.RESIZE);
        this.volumeDao.update(volume);
        BaseOperateParam operateParam = ResizeVolumeOperate.builder().id(UUID.randomUUID().toString())
                .title("更改磁盘大小[" + volume.getName() + "]")
                .volumeId(volume.getVolumeId())
                .size(volume.getCapacity())
                .build();
        operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return volume;
    }

    @Transactional(rollbackFor = Exception.class)
    public MigrateVolumeInfo migrateVolume(int sourceVolumeId, int storageId) {
        StorageEntity storage = this.allocateService.allocateStorage(cn.chenjun.cloud.common.util.Constant.StorageCategory.VOLUME, storageId);
        String volumeType = getVolumeType(storage);
        String volumeName = NameUtil.generateVolumeName();
        VolumeEntity volume = this.findAndUpdateVolumeStatus(sourceVolumeId, cn.chenjun.cloud.common.util.Constant.VolumeStatus.MIGRATE);
        if (volume.getHostId() > 0 && storage.getHostId() > 0 && !Objects.equals(volume.getHostId(), storage.getHostId())) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "跨宿主机本地磁盘无法完成迁移，如需进行操作，请先迁移到共享存储，然后再进行迁移");
        }
        GuestEntity guest = this.getVolumeGuest(sourceVolumeId);
        if (guest != null) {
            switch (guest.getStatus()) {
                case cn.chenjun.cloud.common.util.Constant.GuestStatus.STOP:
                case cn.chenjun.cloud.common.util.Constant.GuestStatus.ERROR:
                    break;
                default:
                    throw new CodeException(ErrorCode.GUEST_NOT_STOP, "当前磁盘所在虚拟机正在运行,请关机后重试");
            }
        }
        volume.setStatus(cn.chenjun.cloud.common.util.Constant.VolumeStatus.MIGRATE);
        this.volumeDao.update(volume);
        VolumeEntity migrateVolume = VolumeEntity.builder()
                .description(volume.getDescription())
                .storageId(storage.getStorageId())
                .hostId(storage.getHostId())
                .templateId(volume.getTemplateId())
                .name(volumeName)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .capacity(volume.getCapacity())
                .allocation(0L)
                .status(cn.chenjun.cloud.common.util.Constant.VolumeStatus.CREATING)
                .deviceId(0)
                .deviceDriver("")
                .guestId(0)
                .createTime(new Date())
                .device(Constant.DeviceType.DISK)
                .serial(DiskSerialUtil.generateDiskSerial())
                .build();
        this.volumeDao.insert(migrateVolume);
        BaseOperateParam operateParam = MigrateVolumeOperate.builder().id(UUID.randomUUID().toString())
                .title("迁移磁盘[" + volume.getName() + "]")
                .sourceVolumeId(volume.getVolumeId())
                .targetVolumeId(migrateVolume.getVolumeId())
                .build();
        operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        this.notifyService.publish(NotifyData.<Void>builder().id(migrateVolume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return MigrateVolumeInfo.builder().source(volume).migrate(migrateVolume).build();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<VolumeEntity> batchDestroyVolume(List<Integer> volumeIds) {
        List<VolumeEntity> models = new ArrayList<>(volumeIds.size());
        for (Integer volumeId : volumeIds) {
            try {
                models.add(this.destroyVolume(volumeId));
            } catch (Exception ignored) {

            }
        }
        return models;
    }

    @Transactional(rollbackFor = Exception.class)
    public VolumeEntity createVolumeTemplate(int volumeId, String name, String arch) {
        this.templateService.createVolumeTemplate(volumeId, name, arch);
        return this.getVolumeById(volumeId);
    }

    @Transactional(rollbackFor = Exception.class)
    public VolumeEntity destroyVolume(int volumeId) {
        VolumeEntity volume = this.volumeDao.findById(volumeId);
        if (volume == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在");
        }
        switch (volume.getStatus()) {
            case cn.chenjun.cloud.common.util.Constant.VolumeStatus.ERROR:
            case cn.chenjun.cloud.common.util.Constant.VolumeStatus.READY: {
                if (volume.getGuestId() > 0) {
                    throw new CodeException(ErrorCode.GUEST_VOLUME_HAS_ATTACH_ERROR, "当前磁盘被系统挂载");
                }
                volume.setStatus(cn.chenjun.cloud.common.util.Constant.VolumeStatus.DESTROY);
                volumeDao.update(volume);
                DestroyVolumeOperate operate = DestroyVolumeOperate.builder().id(UUID.randomUUID().toString()).title("销毁磁盘[" + volume.getName() + "]").volumeId(volumeId).build();
                operateTask.addTask(operate, volume.getStatus() == cn.chenjun.cloud.common.util.Constant.VolumeStatus.ERROR ? 0 : configService.getConfig(ConfigKey.DEFAULT_DESTROY_DELAY_MINUTE));

                this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
                return volume;
            }
            case Constant.VolumeStatus.DESTROY: {
                DestroyVolumeOperate operate = DestroyVolumeOperate.builder().id(UUID.randomUUID().toString()).title("销毁磁盘[" + volume.getName() + "]").volumeId(volumeId).build();
                operateTask.addTask(operate, 0);
                this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
                return volume;
            }
            default:
                throw new CodeException(ErrorCode.VOLUME_NOT_READY, "磁盘当前状态未就绪");
        }
    }

}

