package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.model.TemplateModel;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.CreateVolumeTemplateOperate;
import cn.chenjun.cloud.management.operate.bean.DestroyTemplateOperate;
import cn.chenjun.cloud.management.operate.bean.DownloadTemplateOperate;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.NameUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class TemplateService extends AbstractService {

    @Autowired
    private AllocateService allocateService;


    public ResultUtil<List<TemplateModel>> listTemplate() {
        List<TemplateEntity> templateList = this.templateMapper.selectList(new QueryWrapper<>());
        List<TemplateModel> models = templateList.stream().map(this::initTemplateModel).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    public ResultUtil<Page<TemplateModel>> search(Integer templateType, Integer templateStatus, String keyword, int no, int size) {
        QueryWrapper<TemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(templateType != null, TemplateEntity.TEMPLATE_TYPE, templateType);
        queryWrapper.eq(templateStatus != null, TemplateEntity.TEMPLATE_STATUS, templateStatus);
        if (!ObjectUtils.isEmpty(keyword)) {
            queryWrapper.and(o -> {
                String condition = "%" + keyword + "%";
                QueryWrapper<TemplateEntity> wrapper = o;
                wrapper.like(TemplateEntity.TEMPLATE_NAME, condition);
            });
        }
        int nCount = Math.toIntExact(this.templateMapper.selectCount(queryWrapper));
        int nOffset = (no - 1) * size;
        queryWrapper.last("limit " + nOffset + ", " + size);
        List<TemplateEntity> list = this.templateMapper.selectList(queryWrapper);
        List<TemplateModel> models = list.stream().map(this::initTemplateModel).collect(Collectors.toList());
        Page<TemplateModel> page = Page.create(nCount, nOffset, size);
        page.setList(models);
        return ResultUtil.success(page);
    }
    public ResultUtil<TemplateModel> getTemplateInfo(int templateId) {
        TemplateEntity template = this.templateMapper.selectOne(new QueryWrapper<TemplateEntity>().eq(TemplateEntity.TEMPLATE_ID, templateId));
        if (template == null) {
            return ResultUtil.error(ErrorCode.TEMPLATE_NOT_FOUND, "模版不存在");
        }
        return ResultUtil.success(this.initTemplateModel(template));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<TemplateModel> createTemplate(String name, String uri, String md5, int templateType, String initScript) {
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入模版名称");
        }
        if (StringUtils.isEmpty(uri)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入模版地址");
        }
        TemplateEntity template = TemplateEntity.builder().uri(uri.trim()).name(name.trim()).templateType(templateType).md5(md5.trim()).status(Constant.TemplateStatus.DOWNLOAD).script(initScript).build();
        this.templateMapper.insert(template);
        this.notifyService.publish(NotifyData.<Void>builder().id(template.getTemplateId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_TEMPLATE).build());
        return this.downloadTemplate(template.getTemplateId());
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<TemplateModel> updateTemplateScript(int id, String initScript) {

        TemplateEntity template = templateMapper.selectById(id);
        if (template == null) {
            throw new CodeException(ErrorCode.TEMPLATE_NOT_FOUND, "模版不存在");
        }
        template.setScript(initScript);
        templateMapper.updateById(template);
        this.notifyService.publish(NotifyData.<Void>builder().id(template.getTemplateId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_TEMPLATE).build());
        return ResultUtil.success(this.initTemplateModel(template));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<TemplateModel> downloadTemplate(int templateId) {
        StorageEntity storage = allocateService.allocateStorage(Constant.StorageSupportCategory.TEMPLATE, 0);
        TemplateEntity template = this.templateMapper.selectById(templateId);
        switch (template.getStatus()) {
            case Constant.TemplateStatus.READY:
            case Constant.TemplateStatus.DOWNLOAD:
            case Constant.TemplateStatus.ERROR:
                this.templateVolumeMapper.delete(new QueryWrapper<TemplateVolumeEntity>().eq(TemplateVolumeEntity.TEMPLATE_ID, templateId));
                String volName = NameUtil.generateTemplateVolumeName();
                String volumeType = this.configService.getConfig(ConfigKey.DEFAULT_TEMPLATE_DISK_TYPE);
                if (Objects.equals(template.getTemplateType(), Constant.TemplateType.ISO) || cn.chenjun.cloud.common.util.Constant.StorageType.CEPH_RBD.equals(storage.getType())) {
                    volumeType = cn.chenjun.cloud.common.util.Constant.VolumeType.RAW;
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
                this.templateVolumeMapper.insert(templateVolume);
                template.setStatus(Constant.TemplateStatus.DOWNLOAD);
                this.templateMapper.updateById(template);
                BaseOperateParam operateParam = DownloadTemplateOperate.builder().id(UUID.randomUUID().toString()).title("下载模版[" + template.getName() + "]").templateVolumeId(templateVolume.getTemplateVolumeId()).build();
                operateTask.addTask(operateParam);
                this.notifyService.publish(NotifyData.<Void>builder().id(template.getTemplateId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_TEMPLATE).build());
                return ResultUtil.success(this.initTemplateModel(template));

            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "模版未就绪.");
        }
    }

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

        StorageEntity storage = allocateService.allocateStorage(Constant.StorageSupportCategory.TEMPLATE, 0);
        String volumeType = this.configService.getConfig(ConfigKey.DEFAULT_TEMPLATE_DISK_TYPE);
        if (cn.chenjun.cloud.common.util.Constant.StorageType.CEPH_RBD.equals(storage.getType())) {
            volumeType = cn.chenjun.cloud.common.util.Constant.VolumeType.RAW;
        }
        TemplateEntity template = TemplateEntity.builder().uri(String.valueOf(volumeId))
                .name(name).templateType(Constant.TemplateType.VOLUME)
                .script("")
                .status(Constant.TemplateStatus.CREATING).build();
        this.templateMapper.insert(template);
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
        this.templateVolumeMapper.insert(templateVolume);

        BaseOperateParam operateParam = CreateVolumeTemplateOperate.builder()
                .id(UUID.randomUUID().toString())
                .sourceVolumeId(volumeId)
                .targetTemplateVolumeId(templateVolume.getTemplateVolumeId())
                .title("创建磁盘模版[" + template.getName() + "]")
                .build();
        operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(template.getTemplateId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_TEMPLATE).build());
        this.notifyService.publish(NotifyData.<Void>builder().id(volumeId).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return ResultUtil.success(this.initTemplateModel(template));

    }

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
                BaseOperateParam operate = DestroyTemplateOperate.builder().id(UUID.randomUUID().toString()).title("删除模版[" + template.getName() + "]").templateId(templateId).build();
                operateTask.addTask(operate, configService.getConfig(ConfigKey.DEFAULT_DESTROY_DELAY_MINUTE));
                TemplateModel source = this.initTemplateModel(template);
                this.notifyService.publish(NotifyData.<Void>builder().id(templateId).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_TEMPLATE).build());
                return ResultUtil.success(source);
            default:
                throw new CodeException(ErrorCode.VOLUME_NOT_READY, "快照当前状态未就绪");
        }
    }

}
