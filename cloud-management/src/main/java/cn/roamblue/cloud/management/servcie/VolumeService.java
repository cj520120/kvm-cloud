package cn.roamblue.cloud.management.servcie;

import cn.hutool.core.convert.impl.BeanConverter;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.data.mapper.TemplateMapper;
import cn.roamblue.cloud.management.data.mapper.TemplateVolumeMapper;
import cn.roamblue.cloud.management.data.mapper.VolumeMapper;
import cn.roamblue.cloud.management.model.CloneModel;
import cn.roamblue.cloud.management.model.MigrateModel;
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


    private StorageEntity findStorageById(int clusterId, int storageId) {
        StorageEntity storage;
        if (storageId > 0) {
            storage = storageMapper.selectById(storageId);
            if (storage == null || !Objects.equals(clusterId, storage.getClusterId())) {
                throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
            }
        } else {
            List<StorageEntity> storageList = storageMapper.selectList(new QueryWrapper<StorageEntity>().eq("cluster_id", clusterId));
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
        return new BeanConverter<>(VolumeModel.class).convert(volume, null);
    }

    public ResultUtil<List<VolumeModel>> listVolumes(int clusterId) {
        List<VolumeEntity> volumeList = this.volumeMapper.selectList(new QueryWrapper<VolumeEntity>().eq("cluster_id", clusterId));
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
    public ResultUtil<VolumeModel> createVolume(int clusterId, int storageId, int templateId, String volumeType, long volumeSize) {
        StorageEntity storage = this.findStorageById(clusterId, storageId);
        String volumeName = UUID.randomUUID().toString();
        VolumeEntity volume = VolumeEntity.builder()
                .storageId(storageId)
                .clusterId(clusterId)
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
    public ResultUtil<CloneModel> cloneVolume(int clusterId, int sourceVolumeId, int storageId, String volumeType) {
        StorageEntity storage = this.findStorageById(clusterId, storageId);
        String volumeName = UUID.randomUUID().toString();
        VolumeEntity volume =  this.findAndUpdateVolumeStatus(sourceVolumeId, Constant.VolumeStatus.CLONE);
        VolumeEntity cloneVolume = VolumeEntity.builder()
                .storageId(storageId)
                .clusterId(clusterId)
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
    public ResultUtil<MigrateModel> migrateVolume(int clusterId, int sourceVolumeId, int storageId, String volumeType) {
        StorageEntity storage = this.findStorageById(clusterId, storageId);
        String volumeName = UUID.randomUUID().toString();
        VolumeEntity volume = this.findAndUpdateVolumeStatus(sourceVolumeId, Constant.VolumeStatus.MIGRATE);
        VolumeEntity migrateVolume = VolumeEntity.builder()
                .storageId(storageId)
                .clusterId(clusterId)
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
        switch (volume.getStatus()){
            case  Constant.VolumeStatus.ERROR:
            case  Constant.VolumeStatus.READY:
                volume.setStatus(Constant.VolumeStatus.DESTROY);
                volumeMapper.updateById(volume);
                DestroyVolumeOperate operate=DestroyVolumeOperate.builder().taskId(UUID.randomUUID().toString()).volumeId(volumeId).build();
                operateTask.addTask(operate);
                VolumeModel source =this.initVolume(volume);
                return ResultUtil.success(source);
            default:
                throw new CodeException(ErrorCode.VOLUME_NOT_READY, "磁盘当前状态未就绪");
        }
    }

}

