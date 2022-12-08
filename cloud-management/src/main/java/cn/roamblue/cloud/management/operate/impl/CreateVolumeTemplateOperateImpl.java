package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeCreateTemplateRequest;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.*;
import cn.roamblue.cloud.management.operate.bean.CreateVolumeTemplateOperate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 创建磁盘
 *
 * @author chenjun
 */
@Component
@Slf4j
public class CreateVolumeTemplateOperateImpl extends AbstractOperate<CreateVolumeTemplateOperate, ResultUtil<VolumeInfo>> {

    public CreateVolumeTemplateOperateImpl() {
        super(CreateVolumeTemplateOperate.class);
    }

    @Override
    public void operate(CreateVolumeTemplateOperate param) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.CREATE_TEMPLATE) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if (storage.getStatus() != cn.roamblue.cloud.management.util.Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            TemplateVolumeEntity targetVolume = templateVolumeMapper.selectById(param.getTargetTemplateVolumeId());
            if (targetVolume.getStatus() != cn.roamblue.cloud.management.util.Constant.VolumeStatus.CREATING) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "目标磁盘[" + volume.getName() + "]状态不正常:" + volume.getStatus());
            }
            List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<>());
            Collections.shuffle(hosts);
            HostEntity host = hosts.stream().filter(h -> Objects.equals(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE, h.getStatus())).findFirst().orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "没有可用的主机信息"));
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

    @Override
    public void onFinish(CreateVolumeTemplateOperate param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.CREATE_TEMPLATE) {
            volume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.READY);
            volumeMapper.updateById(volume);
        }
        TemplateVolumeEntity targetVolume = this.templateVolumeMapper.selectById(param.getTargetTemplateVolumeId());
        if (targetVolume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.CREATING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                targetVolume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.READY);
                targetVolume.setAllocation(resultUtil.getData().getAllocation());
                targetVolume.setCapacity(resultUtil.getData().getCapacity());
                this.templateVolumeMapper.updateById(targetVolume);
                TemplateEntity template = this.templateMapper.selectById(targetVolume.getTemplateId());
                template.setStatus(cn.roamblue.cloud.management.util.Constant.TemplateStatus.READY);
                this.templateMapper.updateById(template);
            } else {
                targetVolume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.ERROR);
                this.templateVolumeMapper.deleteById(param.getTargetTemplateVolumeId());

                TemplateEntity template = this.templateMapper.selectById(targetVolume.getTemplateId());
                template.setStatus(cn.roamblue.cloud.management.util.Constant.TemplateStatus.ERROR);
                this.templateMapper.updateById(template);
            }
        }
    }
}
