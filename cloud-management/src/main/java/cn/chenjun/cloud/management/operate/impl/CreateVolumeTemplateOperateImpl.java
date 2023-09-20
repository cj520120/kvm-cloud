package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.NotifyMessage;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeCreateTemplateRequest;
import cn.chenjun.cloud.common.bean.VolumeInfo;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.operate.bean.CreateVolumeTemplateOperate;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class CreateVolumeTemplateOperateImpl extends AbstractOperate<CreateVolumeTemplateOperate, ResultUtil<VolumeInfo>> {

    public CreateVolumeTemplateOperateImpl() {
        super(CreateVolumeTemplateOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(CreateVolumeTemplateOperate param) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.CREATE_TEMPLATE) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if (storage.getStatus() != cn.chenjun.cloud.management.util.Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            TemplateVolumeEntity targetVolume = templateVolumeMapper.selectById(param.getTargetTemplateVolumeId());
            if (targetVolume.getStatus() != cn.chenjun.cloud.management.util.Constant.VolumeStatus.CREATING) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "目标磁盘[" + volume.getName() + "]状态不正常:" + volume.getStatus());
            }
            HostEntity host = this.allocateService.allocateHost(0, 0, 0, 0);
            StorageEntity targetStorage = storageMapper.selectById(targetVolume.getStorageId());
            VolumeCreateTemplateRequest request = VolumeCreateTemplateRequest.builder()
                    .sourceStorage(storage.getName())
                    .sourceVolume(volume.getPath())
                    .targetStorage(targetStorage.getName())
                    .targetName(targetVolume.getName())
                    .targetVolume(targetVolume.getPath())
                    .targetType(targetVolume.getType())
                    .build();

            this.asyncInvoker(host, param, Constant.Command.VOLUME_TEMPLATE, request);
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "原磁盘[" + volume.getName() + "]状态不正常:" + volume.getStatus());
        }

    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<VolumeInfo>>() {
        }.getType();
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(CreateVolumeTemplateOperate param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.CREATE_TEMPLATE) {
            volume.setStatus(cn.chenjun.cloud.management.util.Constant.VolumeStatus.READY);
            volumeMapper.updateById(volume);
        }
        TemplateVolumeEntity targetVolume = this.templateVolumeMapper.selectById(param.getTargetTemplateVolumeId());
        if (targetVolume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.CREATING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                targetVolume.setStatus(cn.chenjun.cloud.management.util.Constant.TemplateStatus.READY);
                targetVolume.setAllocation(resultUtil.getData().getAllocation());
                targetVolume.setCapacity(resultUtil.getData().getCapacity());
                targetVolume.setType(resultUtil.getData().getType());
                targetVolume.setPath(resultUtil.getData().getPath());
                this.templateVolumeMapper.updateById(targetVolume);
                TemplateEntity template = this.templateMapper.selectById(targetVolume.getTemplateId());
                template.setStatus(cn.chenjun.cloud.management.util.Constant.TemplateStatus.READY);
                this.templateMapper.updateById(template);
            } else {
                targetVolume.setStatus(cn.chenjun.cloud.management.util.Constant.TemplateStatus.ERROR);
                this.templateVolumeMapper.deleteById(param.getTargetTemplateVolumeId());

                TemplateEntity template = this.templateMapper.selectById(targetVolume.getTemplateId());
                template.setStatus(cn.chenjun.cloud.management.util.Constant.TemplateStatus.ERROR);
                this.templateMapper.updateById(template);
            }
        }

        this.notifyService.publish(NotifyMessage.builder().id(param.getSourceVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
        this.notifyService.publish(NotifyMessage.builder().id(targetVolume.getTemplateId()).type(Constant.NotifyType.UPDATE_TEMPLATE).build());

    }
}
