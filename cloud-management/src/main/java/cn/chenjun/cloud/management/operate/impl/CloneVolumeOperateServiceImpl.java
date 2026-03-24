package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeCloneRequest;
import cn.chenjun.cloud.common.bean.VolumeInfo;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.operate.bean.CloneVolumeOperate;
import cn.chenjun.cloud.management.util.HostRole;
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
public class CloneVolumeOperateServiceImpl extends AbstractOperateService<CloneVolumeOperate, ResultUtil<VolumeInfo>> {


    @Override
    public void operate(CloneVolumeOperate param) {
        VolumeEntity volume = volumeDao.findById(param.getSourceVolumeId());
        if (volume.getStatus() == Constant.VolumeStatus.CLONE) {
            StorageEntity sourceStorage = storageDao.findById(volume.getStorageId());
            if (sourceStorage.getStatus() != Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            VolumeEntity cloneVolume = volumeDao.findById(param.getTargetVolumeId());
            if (cloneVolume.getStatus() != Constant.VolumeStatus.CREATING) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "目标磁盘[" + volume.getName() + "]状态不正常:" + volume.getStatus());
            }
            HostEntity host = this.allocateService.allocateHost(HostRole.NONE, 0, null, Math.max(sourceStorage.getHostId(), cloneVolume.getHostId()), 0, 0);
            StorageEntity targetStorage = storageDao.findById(cloneVolume.getStorageId());
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
        VolumeEntity volume = volumeDao.findById(param.getSourceVolumeId());

        if (volume != null && volume.getStatus() == Constant.VolumeStatus.CLONE) {
            volume.setStatus(Constant.VolumeStatus.READY);
            volumeDao.update(volume);
        }
        VolumeEntity targetVolume = volumeDao.findById(param.getTargetVolumeId());
        if (targetVolume != null && targetVolume.getStatus() == Constant.VolumeStatus.CREATING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                targetVolume.setStatus(Constant.VolumeStatus.READY);
                targetVolume.setAllocation(resultUtil.getData().getAllocation());
                targetVolume.setCapacity(resultUtil.getData().getCapacity());
                targetVolume.setType(resultUtil.getData().getType());
                targetVolume.setPath(resultUtil.getData().getPath());
            } else {
                targetVolume.setStatus(Constant.VolumeStatus.ERROR);
            }
            volumeDao.update(targetVolume);
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(param.getSourceVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
        this.notifyService.publish(NotifyData.<Void>builder().id(param.getTargetVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
    }

    @Override
    public int getType() {
        return Constant.OperateType.CLONE_VOLUME;
    }
}
