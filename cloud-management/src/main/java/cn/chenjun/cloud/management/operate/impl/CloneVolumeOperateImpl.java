package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeCloneRequest;
import cn.chenjun.cloud.common.bean.VolumeInfo;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.BootstrapType;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.operate.bean.CloneVolumeOperate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class CloneVolumeOperateImpl extends AbstractOperate<CloneVolumeOperate, ResultUtil<VolumeInfo>> {


    @Override
    public void operate(CloneVolumeOperate param) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.CLONE) {
            StorageEntity sourceStorage = storageMapper.selectById(volume.getStorageId());
            if (sourceStorage.getStatus() != cn.chenjun.cloud.management.util.Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            VolumeEntity cloneVolume = volumeMapper.selectById(param.getTargetVolumeId());
            if (cloneVolume.getStatus() != cn.chenjun.cloud.management.util.Constant.VolumeStatus.CREATING) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "目标磁盘[" + volume.getName() + "]状态不正常:" + volume.getStatus());
            }
            HostEntity host = this.allocateService.allocateHost(0, BootstrapType.BIOS, 0, 0, 0);
            StorageEntity targetStorage = storageMapper.selectById(cloneVolume.getStorageId());
            VolumeCloneRequest request = VolumeCloneRequest.builder()
                    .sourceVolume(initVolume(sourceStorage, volume))
                    .targetVolume(initVolume(targetStorage, cloneVolume))
                    .build();

            this.asyncInvoker(host, param, Constant.Command.VOLUME_CLONE, request);
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
    public void onFinish(CloneVolumeOperate param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());

        if (volume != null && volume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.CLONE) {
            volume.setStatus(cn.chenjun.cloud.management.util.Constant.VolumeStatus.READY);
            volumeMapper.updateById(volume);
        }
        VolumeEntity targetVolume = volumeMapper.selectById(param.getTargetVolumeId());
        if (targetVolume != null && targetVolume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.CREATING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                targetVolume.setStatus(cn.chenjun.cloud.management.util.Constant.VolumeStatus.READY);
                targetVolume.setAllocation(resultUtil.getData().getAllocation());
                targetVolume.setCapacity(resultUtil.getData().getCapacity());
                targetVolume.setType(resultUtil.getData().getType());
                targetVolume.setPath(resultUtil.getData().getPath());
            } else {
                targetVolume.setStatus(cn.chenjun.cloud.management.util.Constant.VolumeStatus.ERROR);
            }
            volumeMapper.updateById(targetVolume);
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(param.getSourceVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
        this.notifyService.publish(NotifyData.<Void>builder().id(param.getTargetVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.CLONE_VOLUME;
    }
}
