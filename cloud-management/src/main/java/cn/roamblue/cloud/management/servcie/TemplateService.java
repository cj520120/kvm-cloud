package cn.roamblue.cloud.management.servcie;

import cn.hutool.core.convert.impl.BeanConverter;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeCreateTemplateRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.TemplateEntity;
import cn.roamblue.cloud.management.data.entity.TemplateVolumeEntity;
import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.data.mapper.TemplateMapper;
import cn.roamblue.cloud.management.data.mapper.TemplateVolumeMapper;
import cn.roamblue.cloud.management.data.mapper.VolumeMapper;
import cn.roamblue.cloud.management.model.TemplateModel;
import cn.roamblue.cloud.management.operate.bean.BaseOperateParam;
import cn.roamblue.cloud.management.operate.bean.DownloadTemplateOperate;
import cn.roamblue.cloud.management.task.OperateTask;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class TemplateService {
    @Autowired
    private TemplateMapper templateMapper;
    @Autowired
    private AllocateService allocateService;
    @Autowired
    private OperateTask operateTask;
    @Autowired
    private TemplateVolumeMapper templateVolumeMapper;
    @Autowired
    private VolumeMapper volumeMapper;

    private TemplateModel initTemplateModel(TemplateEntity entity) {
        return new BeanConverter<>(TemplateModel.class).convert(entity, null);

    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<TemplateModel> createTemplate(String name, String uri, int type, String volumeType) {
        TemplateEntity template = TemplateEntity.builder().uri(uri).name(name).type(type).volumeType(volumeType).status(Constant.TemplateStatus.DOWNLOAD).build();
        this.templateMapper.insert(template);
        return this.downloadTemplate(template.getTemplateId());
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<TemplateModel> downloadTemplate(int templateId) {
        StorageEntity storage = allocateService.allocateStorage(0);
        TemplateEntity template = this.templateMapper.selectById(templateId);
        switch (template.getStatus()) {
            case Constant.TemplateStatus.READY:
            case Constant.TemplateStatus.ERROR:
                this.templateVolumeMapper.delete(new QueryWrapper<TemplateVolumeEntity>().eq("template_id", templateId));
                String uid = UUID.randomUUID().toString();
                TemplateVolumeEntity templateVolume = TemplateVolumeEntity.builder()
                        .storageId(storage.getStorageId())
                        .name(uid)
                        .templateId(template.getTemplateId())
                        .path(storage.getMountPath() + "/" + uid)
                        .type(template.getVolumeType())
                        .status(Constant.TemplateStatus.DOWNLOAD)
                        .build();
                this.templateVolumeMapper.insert(templateVolume);
                BaseOperateParam operateParam = DownloadTemplateOperate.builder().taskId(uid).templateVolumeId(templateVolume.getTemplateVolumeId()).build();
                operateTask.addTask(operateParam);
                return ResultUtil.success(this.initTemplateModel(template));

            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "模版未就绪.");
        }
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<TemplateModel> createVolumeTemplate(int volumeId,String name) {
        VolumeEntity volume=this.volumeMapper.selectById(volumeId);
        if(volume.getStatus()!= Constant.VolumeStatus.READY){
            throw new CodeException(ErrorCode.SERVER_ERROR,"当前磁盘状态未就绪");
        }
        volume.setStatus(Constant.VolumeStatus.CREATE_TEMPLATE);
        this.volumeMapper.updateById(volume);
        TemplateEntity template = TemplateEntity.builder().uri(String.valueOf(volumeId))
                .name(name).type(Constant.TemplateType.VOLUME)
                .volumeType(cn.roamblue.cloud.common.util.Constant.VolumeType.QCOW2)
                .status(Constant.TemplateStatus.CREATING).build();
        this.templateMapper.insert(template);
        BaseOperateParam operateParam= null;
        operateTask.addTask(operateParam);
        return ResultUtil.success(this.initTemplateModel(template));

    }

}
