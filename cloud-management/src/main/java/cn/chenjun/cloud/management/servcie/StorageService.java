package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.model.StorageModel;
import cn.chenjun.cloud.management.operate.bean.*;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.NameUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class StorageService extends AbstractService {


    public ResultUtil<List<StorageModel>> listStorage() {
        List<StorageEntity> storageList = this.storageMapper.selectList(new QueryWrapper<>());
        List<StorageModel> models = storageList.stream().map(this::initStorageModel).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    public ResultUtil<StorageModel> getStorageInfo(int storageId) {
        StorageEntity storage = this.storageMapper.selectById(storageId);
        if (storage == null) {
            return ResultUtil.error(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        return ResultUtil.success(this.initStorageModel(storage));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<Void> migrateStorage(int sourceStorageId, int destStorageId) {
        StorageEntity sourceStorage = this.storageMapper.selectById(sourceStorageId);
        StorageEntity destStorage = this.storageMapper.selectById(destStorageId);
        if (sourceStorage == null || (sourceStorage.getStatus() != Constant.StorageStatus.READY && sourceStorage.getStatus() != Constant.StorageStatus.MAINTENANCE)) {
            return ResultUtil.error(ErrorCode.STORAGE_NOT_FOUND, "源存储池未就绪");
        }
        if (destStorage == null || (destStorage.getStatus() != Constant.StorageStatus.READY)) {
            return ResultUtil.error(ErrorCode.STORAGE_NOT_FOUND, "目标存储池未就绪");
        }
        if(sourceStorage.getType().equalsIgnoreCase(cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL)&&destStorage.getType().equalsIgnoreCase(cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL)){
            if(!Objects.equals(sourceStorage.getHostId(),destStorage.getHostId())){
                return ResultUtil.error(ErrorCode.STORAGE_NOT_SUPPORT, "不能跨主机迁移本地存储池");
            }
        }

        String defaultVolumeType = this.configService.getConfig(ConfigKey.DEFAULT_DISK_TYPE);
        if (cn.chenjun.cloud.common.util.Constant.StorageType.CEPH_RBD.equals(destStorage.getType())) {
            defaultVolumeType = cn.chenjun.cloud.common.util.Constant.VolumeType.RAW;
        }
        if ((destStorage.getSupportCategory() & Constant.StorageSupportCategory.VOLUME) == Constant.StorageSupportCategory.VOLUME) {
            List<VolumeEntity> sourceVolumeList = this.volumeMapper.selectList(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.STORAGE_ID, sourceStorageId));
            for (VolumeEntity volume : sourceVolumeList) {
                if (volume.getStatus() != Constant.VolumeStatus.READY) {
                    continue;
                }
                GuestEntity guest = this.getVolumeGuest(volume.getVolumeId());
                if (guest != null && !Objects.equals(guest.getStatus(), Constant.GuestStatus.STOP)) {
                    continue;
                }
                volume.setStatus(Constant.VolumeStatus.MIGRATE);
                this.volumeMapper.updateById(volume);
                String volumeName = NameUtil.generateVolumeName();
                VolumeEntity migrateVolume = VolumeEntity.builder()
                        .description(volume.getDescription())
                        .storageId(destStorageId)
                        .hostId(destStorage.getHostId())
                        .templateId(volume.getTemplateId())
                        .name(volumeName)
                        .path(destStorage.getMountPath() + "/" + volumeName)
                        .type(defaultVolumeType)
                        .capacity(volume.getCapacity())
                        .allocation(0L)
                        .status(Constant.VolumeStatus.CREATING)
                        .createTime(new Date())
                        .build();
                this.volumeMapper.insert(migrateVolume);
                BaseOperateParam operateParam = MigrateVolumeOperate.builder().id(UUID.randomUUID().toString())
                        .title("迁移磁盘[" + volume.getName() + "]")
                        .sourceVolumeId(volume.getVolumeId())
                        .targetVolumeId(migrateVolume.getVolumeId())
                        .build();
                operateTask.addTask(operateParam);
                this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
                this.notifyService.publish(NotifyData.<Void>builder().id(migrateVolume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());

            }
        }
        defaultVolumeType = this.configService.getConfig(ConfigKey.DEFAULT_TEMPLATE_DISK_TYPE);
        if ((destStorage.getSupportCategory() & Constant.StorageSupportCategory.TEMPLATE) == Constant.StorageSupportCategory.TEMPLATE) {
            List<TemplateVolumeEntity> templateVolumeEntityList = this.templateVolumeMapper.selectList(new QueryWrapper<TemplateVolumeEntity>().eq(TemplateVolumeEntity.STORAGE_ID, sourceStorageId));
            for (TemplateVolumeEntity sourceVolume : templateVolumeEntityList) {
                if (sourceVolume.getStatus() != Constant.TemplateStatus.READY) {
                    continue;
                }
                TemplateEntity template = this.templateMapper.selectById(sourceVolume.getTemplateId());
                if (template.getStatus() != Constant.TemplateStatus.READY) {
                    continue;
                }
                String templateVolumeType=defaultVolumeType;
                if(template.getTemplateType().equals(Constant.TemplateType.ISO)){
                    templateVolumeType= cn.chenjun.cloud.common.util.Constant.VolumeType.RAW;
                }
                template.setStatus(Constant.TemplateStatus.MIGRATE);
                this.templateMapper.updateById(template);
                String volumeName = NameUtil.generateTemplateVolumeName();
                TemplateVolumeEntity targetVolume = TemplateVolumeEntity.builder()
                        .storageId(destStorageId)
                        .name(volumeName)
                        .templateId(sourceVolume.getTemplateId())
                        .path(destStorage.getMountPath() + "/" + volumeName)
                        .type(templateVolumeType)
                        .allocation(0L)
                        .capacity(0L)
                        .status(Constant.TemplateStatus.CREATING)
                        .build();
                this.templateVolumeMapper.insert(targetVolume);
                sourceVolume.setStatus(Constant.TemplateStatus.MIGRATE);
                this.templateVolumeMapper.updateById(sourceVolume);
                BaseOperateParam operateParam = MigrateTemplateVolumeOperate.builder().id(UUID.randomUUID().toString())
                        .title("迁移模版磁盘[" + targetVolume.getName() + "]")
                        .sourceTemplateVolumeId(sourceVolume.getTemplateVolumeId())
                        .targetTemplateVolumeId(targetVolume.getTemplateVolumeId())
                        .build();
                operateTask.addTask(operateParam);
                this.notifyService.publish(NotifyData.<Void>builder().id(template.getTemplateId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_TEMPLATE).build());
            }
        }
        return ResultUtil.success();
    }
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<StorageModel> createStorage(int supportCategory, String description, String type, String param) {
        if (StringUtils.isEmpty(description)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入存储池名称");
        }
        if (StringUtils.isEmpty(type)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择存储池类型");
        }
        if(Objects.equals(cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL,type)){
            throw new CodeException(ErrorCode.STORAGE_NOT_SUPPORT,"本地存储池不支持主动创建");
        }
        if (StringUtils.isEmpty(param)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "存储池参数不正确");
        }
        String storageName = UUID.randomUUID().toString().toLowerCase().replace("-", "");
        String mountPath = "";
        switch (type){
            case cn.chenjun.cloud.common.util.Constant.StorageType.NFS:
            case cn.chenjun.cloud.common.util.Constant.StorageType.GLUSTERFS:
                mountPath = "/mnt/" + storageName;
                break;
        }
        StorageEntity storage = StorageEntity.builder()
                .description(description)
                .name(storageName)
                .type(type)
                .hostId(0)
                .param(param)
                .mountPath(mountPath)
                .supportCategory(supportCategory)
                .allocation(0L)
                .capacity(0L)
                .available(0L)
                .status(Constant.StorageStatus.INIT)
                .build();
        this.storageMapper.insert(storage);
        BaseOperateParam operateParam = CreateStorageOperate.builder().id(UUID.randomUUID().toString()).title("创建存储池[" + storage.getName() + "]").storageId(storage.getStorageId()).build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(storage.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());
        return ResultUtil.success(this.initStorageModel(storage));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<StorageModel> registerStorage(int storageId) {
        StorageEntity storage = this.storageMapper.selectById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        switch (storage.getStatus()) {
            case Constant.StorageStatus.READY:
            case Constant.StorageStatus.INIT:
            case Constant.StorageStatus.ERROR:
            case Constant.StorageStatus.MAINTENANCE:
                storage.setStatus(Constant.StorageStatus.INIT);
                this.storageMapper.updateById(storage);
                BaseOperateParam operateParam = CreateStorageOperate.builder().id(UUID.randomUUID().toString()).title("注册存储池[" + storage.getName() + "]").storageId(storage.getStorageId()).build();
                this.operateTask.addTask(operateParam);
                this.notifyService.publish(NotifyData.<Void>builder().id(storage.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());
                return ResultUtil.success(this.initStorageModel(storage));
            default:
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "等待存储池状态就绪");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<StorageModel> updateStorageSupportCategory(int storageId, int supportCategory) {
        StorageEntity storage = this.storageMapper.selectById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        storage.setSupportCategory(supportCategory);
        this.storageMapper.updateById(storage);

        this.notifyService.publish(NotifyData.<Void>builder().id(storage.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());
        return ResultUtil.success(this.initStorageModel(storage));

    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<StorageModel> maintenanceStorage(int storageId) {
        StorageEntity storage = this.storageMapper.selectById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        switch (storage.getStatus()) {
            case Constant.StorageStatus.READY:
            case Constant.StorageStatus.MAINTENANCE:
            case Constant.StorageStatus.ERROR:
                storage.setStatus(Constant.StorageStatus.MAINTENANCE);
                this.storageMapper.updateById(storage);
                this.notifyService.publish(NotifyData.<Void>builder().id(storage.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());
                return ResultUtil.success(this.initStorageModel(storage));
            default:
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "等待存储池状态就绪");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<Void> clearUnLinkVolume(int storageId) {
        StorageEntity storage = this.storageMapper.selectById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        BaseOperateParam operateParam = StorageVolumeCleanOperate.builder().id(UUID.randomUUID().toString()).title("清理存储池[" + storage.getName() + "]").storageId(storage.getStorageId()).build();
        this.operateTask.addTask(operateParam);
        return ResultUtil.success();
    }
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<StorageModel> destroyStorage(int storageId) {
        StorageEntity storage = this.storageMapper.selectById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        switch (storage.getStatus()) {
            case Constant.StorageStatus.READY:
            case Constant.StorageStatus.ERROR:
                if (volumeMapper.selectCount(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.STORAGE_ID, storageId)) > 0) {
                    throw new CodeException(ErrorCode.STORAGE_BUSY, "当前存储有挂载磁盘，请首先迁移存储文件");
                }
                storage.setStatus(Constant.StorageStatus.DESTROY);
                this.storageMapper.updateById(storage);
                BaseOperateParam operateParam = DestroyStorageOperate.builder().id(UUID.randomUUID().toString()).title("销毁存储池[" + storage.getName() + "]").storageId(storage.getStorageId()).build();
                this.operateTask.addTask(operateParam);
                this.notifyService.publish(NotifyData.<Void>builder().id(storage.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());
                return ResultUtil.success(this.initStorageModel(storage));
            default:
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "等待存储池状态就绪");
        }
    }
}
