package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeInfo;
import cn.chenjun.cloud.common.bean.VolumeMigrateRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.TemplateEntity;
import cn.chenjun.cloud.management.data.entity.TemplateVolumeEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyTemplateVolumeOperate;
import cn.chenjun.cloud.management.operate.bean.MigrateTemplateVolumeOperate;
import cn.chenjun.cloud.management.util.HostRole;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.UUID;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class MigrateTemplateVolumeOperateImpl extends AbstractOperate<MigrateTemplateVolumeOperate, ResultUtil<VolumeInfo>> {


    @Override
    public void operate(MigrateTemplateVolumeOperate param) {
        TemplateVolumeEntity volume = templateVolumeMapper.selectById(param.getSourceTemplateVolumeId());
        if (Objects.equals(volume.getStatus(), Constant.TemplateStatus.MIGRATE)) {
            StorageEntity sourceStorage = storageMapper.selectById(volume.getStorageId());
            TemplateVolumeEntity targetVolume = templateVolumeMapper.selectById(param.getTargetTemplateVolumeId());
            if (targetVolume.getStatus() != Constant.TemplateStatus.CREATING) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "目标模版磁盘[" + volume.getName() + "]状态不正常:" + volume.getStatus());
            }
            TemplateEntity template = this.templateMapper.selectById(targetVolume.getTemplateId());
            if (!Objects.equals(template.getStatus(), Constant.TemplateStatus.MIGRATE)) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "模版[" + volume.getName() + "]状态不正常:" + template.getStatus());
            }
            HostEntity host = this.allocateService.allocateHost(HostRole.ALL,0, sourceStorage.getHostId(), 0, 0);
            StorageEntity targetStorage = storageMapper.selectById(targetVolume.getStorageId());
            VolumeMigrateRequest request = VolumeMigrateRequest.builder()
                    .sourceVolume(initVolume(sourceStorage, volume))
                    .targetVolume(initVolume(targetStorage, targetVolume))
                    .build();

            this.asyncInvoker(host, param, Constant.Command.VOLUME_MIGRATE, request);
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "磁盘[" + volume.getName() + "]状态不正常:" + volume.getStatus());
        }

    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<VolumeInfo>>() {
        }.getType();
    }

    @Override
    public void onFinish(MigrateTemplateVolumeOperate param, ResultUtil<VolumeInfo> resultUtil) {
        TemplateVolumeEntity sourceVolume = this.templateVolumeMapper.selectById(param.getSourceTemplateVolumeId());
        TemplateVolumeEntity targetVolume = templateVolumeMapper.selectById(param.getTargetTemplateVolumeId());
        if (sourceVolume == null || targetVolume == null) {
            return;
        }
        TemplateEntity template = this.templateMapper.selectById(targetVolume.getTemplateId());
        if (!Objects.equals(template.getStatus(), Constant.TemplateStatus.MIGRATE)) {
            return;
        }
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            targetVolume.setStatus(Constant.TemplateStatus.READY);
            targetVolume.setAllocation(resultUtil.getData().getAllocation());
            targetVolume.setCapacity(resultUtil.getData().getCapacity());
            targetVolume.setType(resultUtil.getData().getType());
            targetVolume.setPath(resultUtil.getData().getPath());
            templateVolumeMapper.updateById(targetVolume);

            sourceVolume.setStatus(Constant.TemplateStatus.DESTROY);
            templateVolumeMapper.updateById(sourceVolume);
            this.taskService.addTask(DestroyTemplateVolumeOperate.builder().title("删除老模版磁盘").id(UUID.randomUUID().toString()).volumeId(sourceVolume.getTemplateVolumeId()).build());

        } else {
            targetVolume.setStatus(Constant.TemplateStatus.DESTROY);
            templateVolumeMapper.updateById(targetVolume);
            this.taskService.addTask(DestroyTemplateVolumeOperate.builder().title("删除迁移失败的模版磁盘").id(UUID.randomUUID().toString()).volumeId(targetVolume.getTemplateVolumeId()).build());
            sourceVolume.setStatus(Constant.TemplateStatus.READY);
            templateVolumeMapper.updateById(sourceVolume);
        }
        template.setStatus(Constant.TemplateStatus.READY);
        this.templateMapper.updateById(template);
        this.notifyService.publish(NotifyData.<Void>builder().id(template.getTemplateId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_TEMPLATE).build());

    }

    @Override
    public int getType() {
        return Constant.OperateType.MIGRATE_TEMPLATE_VOLUME;
    }
}
