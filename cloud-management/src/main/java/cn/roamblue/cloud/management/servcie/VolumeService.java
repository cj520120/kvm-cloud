package cn.roamblue.cloud.management.servcie;

import cn.hutool.core.convert.impl.BeanConverter;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.TemplateEntity;
import cn.roamblue.cloud.management.data.entity.TemplateVolumeEntity;
import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.data.mapper.TemplateMapper;
import cn.roamblue.cloud.management.data.mapper.TemplateVolumeMapper;
import cn.roamblue.cloud.management.data.mapper.VolumeMapper;
import cn.roamblue.cloud.management.model.VolumeModel;
import cn.roamblue.cloud.management.operate.bean.BaseOperateParam;
import cn.roamblue.cloud.management.operate.bean.CloneVolumeOperate;
import cn.roamblue.cloud.management.operate.bean.CreateVolumeOperate;
import cn.roamblue.cloud.management.task.OperateTask;
import cn.roamblue.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private StorageEntity findStorageById(int clusterId,int storageId){
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

    public ResultUtil<VolumeModel> createVolume(int clusterId, int storageId, int templateId, String volumeType, long volumeSize) {
        StorageEntity storage=this.findStorageById(clusterId, storageId);
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
        VolumeModel model= new BeanConverter<>(VolumeModel.class).convert(volume,null);
        return ResultUtil.success(model);
    }
    public ResultUtil<VolumeModel> cloneVolume(int clusterId, int sourceVolumeId, int storageId, String volumeType, long volumeSize) {
        StorageEntity storage=this.findStorageById(clusterId, storageId);
        String volumeName = UUID.randomUUID().toString();
        VolumeEntity volume = this.volumeMapper.selectById(sourceVolumeId);
        if(volume==null){
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND,"磁盘不存在");
        }
        if(!Objects.equals(volume.getStatus(), Constant.VolumeStatus.READY)){
            throw new CodeException(ErrorCode.VOLUME_NOT_READY,"磁盘当前状态未就绪");
        }
        BaseOperateParam operateParam = CloneVolumeOperate.builder().taskId(volumeName)
                .sourceVolumeId(sourceVolumeId)
                .targetStorageId(storage.getStorageId())
                .targetName(volumeName)
                .targetType(volumeType)
                .targetPath(storage.getMountPath() + "/" + volumeName)
                .build();
        operateTask.addTask(operateParam);
        VolumeModel model= new BeanConverter<>(VolumeModel.class).convert(volume,null);
        return ResultUtil.success(model);
    }
}
