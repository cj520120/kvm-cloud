package cn.roamblue.cloud.management.servcie;

import cn.hutool.core.convert.impl.BeanConverter;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.GuestDiskEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.data.mapper.*;
import cn.roamblue.cloud.management.model.CloneModel;
import cn.roamblue.cloud.management.model.MigrateModel;
import cn.roamblue.cloud.management.model.VolumeAttachModel;
import cn.roamblue.cloud.management.model.VolumeModel;
import cn.roamblue.cloud.management.operate.bean.BaseOperateParam;
import cn.roamblue.cloud.management.operate.bean.CloneVolumeOperate;
import cn.roamblue.cloud.management.operate.bean.CreateVolumeOperate;
import cn.roamblue.cloud.management.operate.bean.DestroyVolumeOperate;
import cn.roamblue.cloud.management.task.OperateTask;
import cn.roamblue.cloud.management.util.Constant;
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
    private GuestDiskMapper guestDiskMapper;


    public StorageEntity findStorageById(int storageId) {
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

    public ResultUtil<List<VolumeModel>> listVolumes() {
        List<VolumeEntity> volumeList = this.volumeMapper.selectList(new QueryWrapper<>());
        List<VolumeModel> models = volumeList.stream().map(this::initVolume).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    public ResultUtil<VolumeModel> getVolumeInfo(int volumeId) {
        VolumeEntity volume = this.volumeMapper.selectById(volumeId);
        if (volume == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在");
        }
        return ResultUtil.success(this.initVolume(volume));
    }


    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<VolumeModel> createVolume(int storageId, int templateId, String volumeType, long volumeSize) {
        StorageEntity storage = this.findStorageById(storageId);
        String volumeName = UUID.randomUUID().toString();
        VolumeEntity volume = VolumeEntity.builder()
                .storageId(storageId)
                .templateId(templateId)
                .name(volumeName)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .capacity(volumeSize)
                .status(Constant.VolumeStatus.CREATING)
                .createTime(new Date())
                .build();
        this.volumeMapper.insert(volume);
        BaseOperateParam operateParam = CreateVolumeOperate.builder().taskId(volumeName).volumeId(volume.getVolumeId()).build();
        operateTask.addTask(operateParam);
        VolumeModel model =this.initVolume(volume);
        return ResultUtil.success(model);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<CloneModel> cloneVolume(int sourceVolumeId, int storageId, String volumeType) {
        StorageEntity storage = this.findStorageById(storageId);
        String volumeName = UUID.randomUUID().toString();
        VolumeEntity volume = this.findAndUpdateVolumeStatus(sourceVolumeId, Constant.VolumeStatus.CLONE);
        VolumeEntity cloneVolume = VolumeEntity.builder()
                .storageId(storageId)
                .templateId(volume.getTemplateId())
                .name(volumeName)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .capacity(volume.getCapacity())
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

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<MigrateModel> migrateVolume(int sourceVolumeId, int storageId, String volumeType) {
        StorageEntity storage = this.findStorageById(storageId);
        String volumeName = UUID.randomUUID().toString();
        VolumeEntity volume = this.findAndUpdateVolumeStatus(sourceVolumeId, Constant.VolumeStatus.MIGRATE);
        VolumeEntity migrateVolume = VolumeEntity.builder()
                .storageId(storageId)
                .templateId(volume.getTemplateId())
                .name(volumeName)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .capacity(volume.getCapacity())
                .status(Constant.VolumeStatus.CREATING)
                .createTime(new Date())
                .build();
        this.volumeMapper.insert(migrateVolume);
        BaseOperateParam operateParam = CloneVolumeOperate.builder().taskId(volumeName)
                .sourceVolumeId(volume.getVolumeId())
                .targetVolumeId(migrateVolume.getVolumeId())
                .build();
        operateTask.addTask(operateParam);
        VolumeModel source = this.initVolume(volume);
        VolumeModel migrate =this.initVolume(migrateVolume);
        return ResultUtil.success(MigrateModel.builder().source(source).migrate(migrate).build());
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<VolumeModel> destroyVolume(int volumeId) {
        VolumeEntity volume = this.volumeMapper.selectById(volumeId);
        if (volume == null) {
           return ResultUtil.error(ErrorCode.VOLUME_NOT_FOUND,"磁盘不存在");
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

}

