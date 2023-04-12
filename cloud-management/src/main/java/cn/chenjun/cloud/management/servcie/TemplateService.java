package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.NotifyInfo;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.model.TemplateModel;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.CreateVolumeTemplateOperate;
import cn.chenjun.cloud.management.operate.bean.DestroyTemplateOperate;
import cn.chenjun.cloud.management.operate.bean.DownloadTemplateOperate;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class TemplateService extends AbstractService {

    @Autowired
    private AllocateService allocateService;

    private GuestEntity getVolumeGuest(int volumeId) {
        GuestDiskEntity guestDisk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq("volume_id", volumeId));
        if (guestDisk == null) {
            return null;
        }
        GuestEntity guest = guestMapper.selectById(guestDisk.getGuestId());
        return guest;
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    public ResultUtil<List<TemplateModel>> listTemplate() {
        List<TemplateEntity> templateList = this.templateMapper.selectList(new QueryWrapper<>());
        List<TemplateModel> models = templateList.stream().map(this::initTemplateModel).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    public ResultUtil<TemplateModel> getTemplateInfo(int templateId) {
        TemplateEntity template = this.templateMapper.selectOne(new QueryWrapper<TemplateEntity>().eq("template_id", templateId));
        if (template == null) {
            throw new CodeException(ErrorCode.TEMPLATE_NOT_FOUND, "模版不存在");
        }
        return ResultUtil.success(this.initTemplateModel(template));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<TemplateModel> createTemplate(String name, String uri, int templateType, String volumeType) {
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入模版名称");
        }
        if (StringUtils.isEmpty(uri)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入模版地址");
        }
        if (StringUtils.isEmpty(volumeType)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入磁盘类型");
        }
        TemplateEntity template = TemplateEntity.builder().uri(uri).name(name).templateType(templateType).volumeType(volumeType).status(Constant.TemplateStatus.DOWNLOAD).build();
        this.templateMapper.insert(template);
        this.notifyService.publish(NotifyInfo.builder().id(template.getTemplateId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_TEMPLATE).build());
        return this.downloadTemplate(template.getTemplateId());
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<TemplateModel> downloadTemplate(int templateId) {
        StorageEntity storage = allocateService.allocateStorage(0);
        TemplateEntity template = this.templateMapper.selectById(templateId);
        switch (template.getStatus()) {
            case Constant.TemplateStatus.READY:
            case Constant.TemplateStatus.DOWNLOAD:
            case Constant.TemplateStatus.ERROR:
                this.templateVolumeMapper.delete(new QueryWrapper<TemplateVolumeEntity>().eq("template_id", templateId));
                String uid = UUID.randomUUID().toString();
                TemplateVolumeEntity templateVolume = TemplateVolumeEntity.builder()
                        .storageId(storage.getStorageId())
                        .name(uid)
                        .templateId(template.getTemplateId())
                        .path(storage.getMountPath() + "/" + uid)
                        .type(template.getVolumeType())
                        .allocation(0L)
                        .capacity(0L)
                        .status(Constant.TemplateStatus.DOWNLOAD)
                        .build();
                this.templateVolumeMapper.insert(templateVolume);
                template.setStatus(Constant.TemplateStatus.DOWNLOAD);
                this.templateMapper.updateById(template);
                BaseOperateParam operateParam = DownloadTemplateOperate.builder().taskId(uid).title("下载模版[" + template.getName() + "]").templateVolumeId(templateVolume.getTemplateVolumeId()).build();
                operateTask.addTask(operateParam);
                this.notifyService.publish(NotifyInfo.builder().id(template.getTemplateId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_TEMPLATE).build());
                return ResultUtil.success(this.initTemplateModel(template));

            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "模版未就绪.");
        }
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<TemplateModel> createVolumeTemplate(int volumeId, String name) {
        VolumeEntity volume = this.volumeMapper.selectById(volumeId);
        if (volume.getStatus() != Constant.VolumeStatus.READY) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘状态未就绪");
        }
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
        volume.setStatus(Constant.VolumeStatus.CREATE_TEMPLATE);
        this.volumeMapper.updateById(volume);
        TemplateEntity template = TemplateEntity.builder().uri(String.valueOf(volumeId))
                .name(name).templateType(Constant.TemplateType.VOLUME)
                .volumeType(cn.chenjun.cloud.common.util.Constant.VolumeType.QCOW2)
                .status(Constant.TemplateStatus.CREATING).build();
        this.templateMapper.insert(template);
        String uid = UUID.randomUUID().toString();
        StorageEntity storage = allocateService.allocateStorage(0);
        TemplateVolumeEntity templateVolume = TemplateVolumeEntity.builder()
                .storageId(storage.getStorageId())
                .name(uid)
                .templateId(template.getTemplateId())
                .path(storage.getMountPath() + "/" + uid)
                .type(template.getVolumeType())
                .capacity(0L)
                .allocation(0L)
                .status(Constant.TemplateStatus.CREATING)
                .build();
        this.templateVolumeMapper.insert(templateVolume);

        BaseOperateParam operateParam = CreateVolumeTemplateOperate.builder()
                .taskId(uid)
                .sourceVolumeId(volumeId)
                .targetTemplateVolumeId(templateVolume.getTemplateVolumeId())
                .title("创建磁盘模版[" + template.getName() + "]")
                .build();
        operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyInfo.builder().id(template.getTemplateId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_TEMPLATE).build());
        this.notifyService.publish(NotifyInfo.builder().id(volumeId).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return ResultUtil.success(this.initTemplateModel(template));

    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<TemplateModel> destroyTemplate(int templateId) {
        TemplateEntity template = this.templateMapper.selectById(templateId);
        if (template == null) {
            return ResultUtil.error(ErrorCode.TEMPLATE_NOT_FOUND, "模版不存在");
        }
        switch (template.getStatus()) {
            case Constant.TemplateStatus.ERROR:
            case Constant.TemplateStatus.READY:
            case Constant.TemplateStatus.DESTROY:
                template.setStatus(Constant.TemplateStatus.DESTROY);
                this.templateMapper.updateById(template);
                BaseOperateParam operate = DestroyTemplateOperate.builder().taskId(UUID.randomUUID().toString()).title("删除模版[" + template.getName() + "]").templateId(templateId).build();
                operateTask.addTask(operate);
                TemplateModel source = this.initTemplateModel(template);
                this.notifyService.publish(NotifyInfo.builder().id(templateId).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_TEMPLATE).build());
                return ResultUtil.success(source);
            default:
                throw new CodeException(ErrorCode.VOLUME_NOT_READY, "快照当前状态未就绪");
        }
    }

}
