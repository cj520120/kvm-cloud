package cn.ruiyun.cloud.management.servcie;

import cn.ruiyun.cloud.common.bean.Page;
import cn.ruiyun.cloud.common.core.operate.BaseOperateParam;
import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.management.data.entity.*;
import cn.ruiyun.cloud.management.data.entity.*;
import cn.ruiyun.cloud.management.operate.bean.CreateVolumeTemplateOperate;
import cn.ruiyun.cloud.management.operate.bean.DestroyTemplateOperate;
import cn.ruiyun.cloud.management.operate.bean.DownloadTemplateOperate;
import cn.ruiyun.cloud.management.util.ConfigKey;
import cn.ruiyun.cloud.management.util.NameUtil;
import cn.ruiyun.cloud.management.util.NotifyContextHolderUtil;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author chenjun
 */
@Service
public class TemplateService extends AbstractService {

    @Autowired
    private AllocateService allocateService;


    public List<TemplateEntity> listTemplate() {
        List<TemplateEntity> templateList = this.templateDao.listAll();
        return templateList;
    }

    public Page<TemplateEntity> search(Integer templateType, Integer templateStatus, String keyword, int no, int size) {

        Page<TemplateEntity> page = this.templateDao.search(templateType, templateStatus, keyword, no, size);
        return page;
    }

    public TemplateEntity getTemplateById(int templateId) {
        TemplateEntity template = this.templateDao.findById(templateId);
        if (template == null) {
            throw new CodeException(ErrorCode.TEMPLATE_NOT_FOUND, "Template not found.");
        }
        return template;
    }

    @Transactional(rollbackFor = Exception.class)
    public TemplateEntity createTemplate(String name, String uri, String md5, int templateType, String arch, String localCloudCfg, String vendorData, int cloudWaitFlag) {
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "Please input template name.");
        }
        if (StringUtils.isEmpty(uri)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "Please input template uri.");
        }
        TemplateEntity template = TemplateEntity.builder().uri(uri.trim()).name(name.trim()).
                templateType(templateType)
                .arch(arch)
                .md5(md5.trim())
                .localCloudCfg(localCloudCfg)
                .cloudWaitFlag(cloudWaitFlag)
                .vendorData(vendorData)
                .status(Constant.TemplateStatus.DOWNLOAD)
                .build();
        this.templateDao.insert(template);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(template.getTemplateId()).type(Constant.NotifyType.UPDATE_TEMPLATE).build());
        return this.downloadTemplate(template.getTemplateId());
    }

    @Transactional(rollbackFor = Exception.class)
    public TemplateEntity updateTemplateScript(int id, String localCloudCfg, String vendorData, int cloudWaitFlag) {

        TemplateEntity template = templateDao.findById(id);
        if (template == null) {
            throw new CodeException(ErrorCode.TEMPLATE_NOT_FOUND, "Template not found.");
        }
        template.setVendorData(vendorData);
        template.setLocalCloudCfg(localCloudCfg);
        template.setCloudWaitFlag(cloudWaitFlag);
        templateDao.update(template);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(template.getTemplateId()).type(Constant.NotifyType.UPDATE_TEMPLATE).build());
        return template;
    }

    @Transactional(rollbackFor = Exception.class)
    public TemplateEntity downloadTemplate(int templateId) {
        StorageEntity storage = allocateService.allocateStorage(Constant.StorageCategory.TEMPLATE, 0);
        TemplateEntity template = this.templateDao.findById(templateId);
        switch (template.getStatus()) {
            case Constant.TemplateStatus.READY:
            case Constant.TemplateStatus.DOWNLOAD:
            case Constant.TemplateStatus.ERROR:
                this.templateVolumeDao.deleteByTemplateId(template.getTemplateId());
                String volName = NameUtil.generateTemplateVolumeName();
                String volumeType = this.configService.getConfig(ConfigKey.DEFAULT_TEMPLATE_DISK_TYPE);
                if (Objects.equals(template.getTemplateType(), Constant.TemplateType.ISO) || Constant.StorageType.CEPH_RBD.equals(storage.getType())) {
                    volumeType = Constant.VolumeType.RAW;
                }
                TemplateVolumeEntity templateVolume = TemplateVolumeEntity.builder()
                        .storageId(storage.getStorageId())
                        .name(volName)
                        .templateId(template.getTemplateId())
                        .path(storage.getMountPath() + "/" + volName)
                        .type(volumeType)
                        .allocation(0L)
                        .capacity(0L)
                        .status(Constant.TemplateStatus.DOWNLOAD)
                        .build();
                this.templateVolumeDao.insert(templateVolume);
                template.setStatus(Constant.TemplateStatus.DOWNLOAD);
                this.templateDao.update(template);
                BaseOperateParam operateParam = DownloadTemplateOperate.builder().id(UUID.randomUUID().toString()).title("下载模版[" + template.getName() + "]").templateVolumeId(templateVolume.getTemplateVolumeId()).build();
                operateTask.addTask(operateParam);
                NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(template.getTemplateId()).type(Constant.NotifyType.UPDATE_TEMPLATE).build());
                return template;

            default:
                throw new CodeException(ErrorCode.TEMPLATE_NOT_READY, "模版未就绪.");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public TemplateEntity createVolumeTemplate(int volumeId, String name, String arch) {
        VolumeEntity volume = this.volumeDao.findById(volumeId);
        if (volume.getStatus() != Constant.VolumeStatus.READY) {
            throw new CodeException(ErrorCode.VOLUME_NOT_READY, "当前磁盘状态未就绪");
        }
        if (volume.getGuestId() > 0) {
            GuestEntity guest = this.getVolumeGuest(volumeId);
            if (guest != null) {
                switch (guest.getStatus()) {
                    case Constant.GuestStatus.STOP:
                    case Constant.GuestStatus.ERROR:
                        break;
                    default:
                        throw new CodeException(ErrorCode.GUEST_NOT_STOP, "当前磁盘所在虚拟机正在运行,请关机后重试");
                }
            }
        }
        volume.setStatus(Constant.VolumeStatus.CREATE_TEMPLATE);
        this.volumeDao.update(volume);

        StorageEntity storage = allocateService.allocateStorage(Constant.StorageCategory.TEMPLATE, 0);
        String volumeType = this.configService.getConfig(ConfigKey.DEFAULT_TEMPLATE_DISK_TYPE);
        if (Constant.StorageType.CEPH_RBD.equals(storage.getType())) {
            volumeType = Constant.VolumeType.RAW;
        }
        TemplateEntity template = TemplateEntity.builder().uri(String.valueOf(volumeId))
                .name(name).templateType(Constant.TemplateType.VOLUME)
                .arch(arch)
                .vendorData("")
                .localCloudCfg("")
                .md5("")
                .cloudWaitFlag(Constant.CloudWaitFlag.NO)
                .status(Constant.TemplateStatus.CREATING).build();
        this.templateDao.insert(template);
        String volumeName = NameUtil.generateTemplateVolumeName();
        TemplateVolumeEntity templateVolume = TemplateVolumeEntity.builder()
                .storageId(storage.getStorageId())
                .name(volumeName)
                .templateId(template.getTemplateId())
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .capacity(0L)
                .allocation(0L)
                .status(Constant.TemplateStatus.CREATING)
                .build();
        this.templateVolumeDao.insert(templateVolume);

        BaseOperateParam operateParam = CreateVolumeTemplateOperate.builder()
                .id(UUID.randomUUID().toString())
                .sourceVolumeId(volumeId)
                .targetTemplateVolumeId(templateVolume.getTemplateVolumeId())
                .title("创建磁盘模版[" + template.getName() + "]")
                .build();
        operateTask.addTask(operateParam);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(template.getTemplateId()).type(Constant.NotifyType.UPDATE_TEMPLATE).build());
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(volumeId).type(Constant.NotifyType.UPDATE_VOLUME).build());
        return template;

    }

    @Transactional(rollbackFor = Exception.class)
    public TemplateEntity destroyTemplate(int templateId) {
        TemplateEntity template = this.templateDao.findById(templateId);
        if (template == null) {
            throw new CodeException(ErrorCode.TEMPLATE_NOT_FOUND, "模版不存在");
        }
        int timeout = 0;
        boolean bUpdateTask = false;
        switch (template.getStatus()) {
            case Constant.TemplateStatus.DESTROY:
            case Constant.TemplateStatus.ERROR:
                bUpdateTask = true;
                break;
            default:
                timeout = configService.getConfig(ConfigKey.DEFAULT_DESTROY_DELAY_MINUTE);
                break;
        }
        template.setStatus(Constant.TemplateStatus.DESTROY);
        this.templateDao.update(template);
        BaseOperateParam operate = DestroyTemplateOperate.builder().id(UUID.randomUUID().toString()).title("删除模版[" + template.getName() + "]").templateId(templateId).build();
        operateTask.addTask(operate, timeout, bUpdateTask);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(templateId).type(Constant.NotifyType.UPDATE_TEMPLATE).build());
        return template;

    }

    public List<TemplateEntity> listTemplateByIds(List<Integer> templateIds) {
        return this.templateDao.listByIds(templateIds);
    }
}
