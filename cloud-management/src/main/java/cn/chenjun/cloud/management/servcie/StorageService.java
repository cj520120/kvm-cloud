package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.operate.bean.*;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.DiskSerialUtil;
import cn.chenjun.cloud.management.util.NameUtil;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class StorageService extends AbstractHostStorageService {


    public List<StorageEntity> listStorage() {
        List<StorageEntity> storageList = this.storageDao.listAll();
        return storageList;
    }

    public Page<StorageEntity> search(Integer storageType, Integer storageStatus, String keyword, int no, int size) {

        Page<StorageEntity> page = this.storageDao.search(storageType, storageStatus, keyword, no, size);
        return page;
    }

    public StorageEntity getStorageInfo(int storageId) {
        StorageEntity storage = this.storageDao.findById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        return storage;
    }

    @Transactional(rollbackFor = Exception.class)
    public StorageEntity migrateStorage(int sourceStorageId, int destStorageId) {
        StorageEntity sourceStorage = this.storageDao.findById(sourceStorageId);
        StorageEntity destStorage = this.storageDao.findById(destStorageId);
        if (sourceStorage == null || (sourceStorage.getStatus() != cn.chenjun.cloud.common.util.Constant.StorageStatus.READY && sourceStorage.getStatus() != cn.chenjun.cloud.common.util.Constant.StorageStatus.MAINTENANCE)) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "源存储池未就绪");
        }
        if (destStorage == null || (destStorage.getStatus() != cn.chenjun.cloud.common.util.Constant.StorageStatus.READY)) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "目标存储池未就绪");
        }
        if (sourceStorage.getType().equalsIgnoreCase(cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL) && destStorage.getType().equalsIgnoreCase(cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL)) {
            if (!Objects.equals(sourceStorage.getHostId(), destStorage.getHostId())) {
                throw new CodeException(ErrorCode.STORAGE_NOT_SUPPORT, "不能跨主机迁移本地存储池");
            }
        }

        String defaultVolumeType = this.configService.getConfig(ConfigKey.DEFAULT_DISK_TYPE);
        if (cn.chenjun.cloud.common.util.Constant.StorageType.CEPH_RBD.equals(destStorage.getType())) {
            defaultVolumeType = cn.chenjun.cloud.common.util.Constant.VolumeType.RAW;
        }
        if ((destStorage.getSupportCategory() & cn.chenjun.cloud.common.util.Constant.StorageCategory.VOLUME) == cn.chenjun.cloud.common.util.Constant.StorageCategory.VOLUME) {
            List<VolumeEntity> sourceVolumeList = this.volumeDao.listByStorageId(sourceStorageId);
            for (VolumeEntity volume : sourceVolumeList) {
                if (volume.getStatus() != cn.chenjun.cloud.common.util.Constant.VolumeStatus.READY) {
                    continue;
                }
                GuestEntity guest = this.getVolumeGuest(volume.getVolumeId());
                if (guest != null && !Objects.equals(guest.getStatus(), cn.chenjun.cloud.common.util.Constant.GuestStatus.STOP)) {
                    continue;
                }
                volume.setStatus(cn.chenjun.cloud.common.util.Constant.VolumeStatus.MIGRATE);
                this.volumeDao.update(volume);
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
                NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
                NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(migrateVolume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());

            }
        }
        defaultVolumeType = this.configService.getConfig(ConfigKey.DEFAULT_TEMPLATE_DISK_TYPE);
        if ((destStorage.getSupportCategory() & cn.chenjun.cloud.common.util.Constant.StorageCategory.TEMPLATE) == cn.chenjun.cloud.common.util.Constant.StorageCategory.TEMPLATE) {
            List<TemplateVolumeEntity> templateVolumeEntityList = this.templateVolumeDao.listByStorageId(sourceStorageId);
            for (TemplateVolumeEntity sourceVolume : templateVolumeEntityList) {
                if (sourceVolume.getStatus() != cn.chenjun.cloud.common.util.Constant.TemplateStatus.READY) {
                    continue;
                }
                TemplateEntity template = this.templateDao.findById(sourceVolume.getTemplateId());
                if (template.getStatus() != cn.chenjun.cloud.common.util.Constant.TemplateStatus.READY) {
                    continue;
                }
                String templateVolumeType = defaultVolumeType;
                if (template.getTemplateType().equals(cn.chenjun.cloud.common.util.Constant.TemplateType.ISO)) {
                    templateVolumeType = cn.chenjun.cloud.common.util.Constant.VolumeType.RAW;
                }
                template.setStatus(cn.chenjun.cloud.common.util.Constant.TemplateStatus.MIGRATE);
                this.templateDao.update(template);
                String volumeName = NameUtil.generateTemplateVolumeName();
                TemplateVolumeEntity targetVolume = TemplateVolumeEntity.builder()
                        .storageId(destStorageId)
                        .name(volumeName)
                        .templateId(sourceVolume.getTemplateId())
                        .path(destStorage.getMountPath() + "/" + volumeName)
                        .type(templateVolumeType)
                        .allocation(0L)
                        .capacity(0L)
                        .status(cn.chenjun.cloud.common.util.Constant.TemplateStatus.CREATING)
                        .build();
                this.templateVolumeDao.insert(targetVolume);
                sourceVolume.setStatus(cn.chenjun.cloud.common.util.Constant.TemplateStatus.MIGRATE);
                this.templateVolumeDao.update(sourceVolume);
                BaseOperateParam operateParam = MigrateTemplateVolumeOperate.builder().id(UUID.randomUUID().toString())
                        .title("迁移模版磁盘[" + targetVolume.getName() + "]")
                        .sourceTemplateVolumeId(sourceVolume.getTemplateVolumeId())
                        .targetTemplateVolumeId(targetVolume.getTemplateVolumeId())
                        .build();
                operateTask.addTask(operateParam);
                NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(template.getTemplateId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_TEMPLATE).build());
            }
        }
        return this.getStorageInfo(sourceStorageId);
    }

    @Transactional(rollbackFor = Exception.class)
    public StorageEntity createStorage(int supportCategory, String description, String type, String param) {
        if (StringUtils.isEmpty(description)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入存储池名称");
        }
        if (StringUtils.isEmpty(type)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择存储池类型");
        }
        if (StringUtils.isEmpty(param)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "存储池参数不正确");
        }
        String storageName = UUID.randomUUID().toString().toLowerCase().replace("-", "");
        String mountPath;
        switch (type) {
            case cn.chenjun.cloud.common.util.Constant.StorageType.NFS:
            case cn.chenjun.cloud.common.util.Constant.StorageType.GLUSTERFS:
                mountPath = "/mnt/" + storageName;
                break;
            case Constant.StorageType.LOCAL:
                Map<String, Object> paramMap = GsonBuilderUtil.create().fromJson(param, new TypeToken<Map<String, Object>>() {
                });
                mountPath = (String) paramMap.get("path");
                break;
            default:
                mountPath = "";
                break;
        }
        StorageEntity storage = StorageEntity.builder()
                .description(description)
                .name(storageName)
                .type(type)
                .hostId(0)
                .parentId(0)
                .param(param)
                .mountPath(mountPath)
                .supportCategory(supportCategory)
                .allocation(0L)
                .capacity(0L)
                .available(0L)
                .status(cn.chenjun.cloud.common.util.Constant.StorageStatus.INIT)
                .build();
        this.storageDao.insert(storage);
        if (storage.getType().equalsIgnoreCase(cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL)) {
            // 如果是本地存储，则需要在所有主机上注册
            List<HostEntity> hostList = this.hostDao.listAll();
            for (HostEntity host : hostList) {
                this.checkAndInitHostLocalStorage(storage, host);
            }
        }
        BaseOperateParam operateParam = CreateStorageOperate.builder().id(UUID.randomUUID().toString()).title("创建存储池[" + storage.getName() + "]").storageId(storage.getStorageId()).build();
        this.operateTask.addTask(operateParam);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(storage.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());
        return storage;
    }

    @Transactional(rollbackFor = Exception.class)
    public StorageEntity registerStorage(int storageId) {
        StorageEntity storage = this.storageDao.findById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }


        switch (storage.getStatus()) {
            case cn.chenjun.cloud.common.util.Constant.StorageStatus.READY:
            case cn.chenjun.cloud.common.util.Constant.StorageStatus.INIT:
            case cn.chenjun.cloud.common.util.Constant.StorageStatus.ERROR:
            case cn.chenjun.cloud.common.util.Constant.StorageStatus.MAINTENANCE:
                updateStorageStatus(storage, cn.chenjun.cloud.common.util.Constant.StorageStatus.INIT);
                BaseOperateParam operateParam = CreateStorageOperate.builder().id(UUID.randomUUID().toString()).title("注册存储池[" + storage.getName() + "]").storageId(storage.getStorageId()).build();
                this.operateTask.addTask(operateParam);
                NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(storage.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());
                return storage;
            default:
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "等待存储池状态就绪");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public StorageEntity updateStorageSupportCategory(int storageId, int supportCategory) {
        StorageEntity storage = this.storageDao.findById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        storage.setSupportCategory(supportCategory);
        this.storageDao.update(storage);

        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(storage.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());
        return storage;

    }

    @Transactional(rollbackFor = Exception.class)
    public StorageEntity maintenanceStorage(int storageId) {
        StorageEntity storage = this.storageDao.findById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        switch (storage.getStatus()) {
            case cn.chenjun.cloud.common.util.Constant.StorageStatus.READY:
            case cn.chenjun.cloud.common.util.Constant.StorageStatus.MAINTENANCE:
            case cn.chenjun.cloud.common.util.Constant.StorageStatus.ERROR:
                updateStorageStatus(storage, Constant.StorageStatus.MAINTENANCE);
                return storage;
            default:
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "等待存储池状态就绪");
        }
    }

    private void updateStorageStatus(StorageEntity storage, int status) {
        storage.setStatus(status);
        this.storageDao.update(storage);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(storage.getStorageId()).type(Constant.NotifyType.UPDATE_STORAGE).build());


        if (storage.getType().equalsIgnoreCase(Constant.StorageType.LOCAL) && storage.getHostId() == 0) {
            // 如果是本地存储，则需要在所有主机上注册
            List<StorageEntity> childStorageList = this.storageDao.listStorageByParentStorageId(storage.getStorageId());
            for (StorageEntity childStorage : childStorageList) {
                childStorage.setStatus(status);
                this.storageDao.update(childStorage);
                NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(childStorage.getStorageId()).type(Constant.NotifyType.UPDATE_STORAGE).build());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public StorageEntity clearUnLinkVolume(int storageId) {
        StorageEntity storage = this.storageDao.findById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        BaseOperateParam operateParam = StorageVolumeCleanOperate.builder().id(UUID.randomUUID().toString()).title("清理存储池[" + storage.getName() + "]").storageId(storage.getStorageId()).build();
        this.operateTask.addTask(operateParam);
        return this.getStorageInfo(storageId);
    }

    @Transactional(rollbackFor = Exception.class)
    public StorageEntity destroyStorage(int storageId) {
        StorageEntity storage = this.storageDao.findById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        if (storage.getType().equals(cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL) && storage.getHostId() > 0) {
            throw new CodeException(ErrorCode.STORAGE_NOT_SUPPORT, "本地存储不支持单独销毁");
        }
        if (volumeDao.countByStorageId(storageId) > 0) {
            throw new CodeException(ErrorCode.STORAGE_HAS_VOLUME, "当前存储有挂载磁盘，请首先迁移存储文件");
        }
        if (storage.getType().equals(cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL)) {
            List<StorageEntity> childStorageList = this.storageDao.listStorageByParentStorageId(storage.getStorageId());
            for (StorageEntity childStorage : childStorageList) {
                if (volumeDao.countByStorageId(childStorage.getStorageId()) > 0) {
                    throw new CodeException(ErrorCode.STORAGE_HAS_VOLUME, "当前存储有挂载磁盘，请首先迁移存储文件");
                }
            }
        }
        switch (storage.getStatus()) {
            case cn.chenjun.cloud.common.util.Constant.StorageStatus.INIT:
            case cn.chenjun.cloud.common.util.Constant.StorageStatus.READY:
            case cn.chenjun.cloud.common.util.Constant.StorageStatus.MAINTENANCE:
            case cn.chenjun.cloud.common.util.Constant.StorageStatus.ERROR:
                storage.setStatus(cn.chenjun.cloud.common.util.Constant.StorageStatus.DESTROY);
                this.storageDao.update(storage);
                BaseOperateParam operateParam = DestroyStorageOperate.builder().id(UUID.randomUUID().toString()).title("销毁存储池[" + storage.getName() + "]").storageId(storage.getStorageId()).build();
                this.operateTask.addTask(operateParam);
                NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(storage.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());

                if (storage.getType().equals(cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL)) {
                    List<StorageEntity> childStorageList = this.storageDao.listStorageByParentStorageId(storage.getStorageId());
                    for (StorageEntity childStorage : childStorageList) {
                        childStorage.setStatus(cn.chenjun.cloud.common.util.Constant.StorageStatus.DESTROY);
                        this.storageDao.update(childStorage);
                        BaseOperateParam childOperateParam = DestroyStorageOperate.builder().id(UUID.randomUUID().toString()).title("销毁存储池[" + childStorage.getName() + "]").storageId(childStorage.getStorageId()).build();
                        this.operateTask.addTask(childOperateParam);
                        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(childStorage.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());
                    }
                }
                return storage;
            default:
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "等待存储池状态就绪");
        }
    }

    public List<StorageEntity> listLocalStorage() {
        return this.storageDao.listLocalStorage();
    }
}
