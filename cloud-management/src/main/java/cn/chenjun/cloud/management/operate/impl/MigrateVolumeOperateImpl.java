package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeInfo;
import cn.chenjun.cloud.common.bean.VolumeMigrateRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestDiskEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyVolumeOperate;
import cn.chenjun.cloud.management.operate.bean.MigrateVolumeOperate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.UUID;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class MigrateVolumeOperateImpl extends AbstractOperate<MigrateVolumeOperate, ResultUtil<VolumeInfo>> {


    @Override
    public void operate(MigrateVolumeOperate param) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.MIGRATE) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if (storage.getStatus() != cn.chenjun.cloud.management.util.Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            VolumeEntity targetVolume = volumeMapper.selectById(param.getTargetVolumeId());
            if (targetVolume.getStatus() != cn.chenjun.cloud.management.util.Constant.VolumeStatus.CREATING) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "目标磁盘[" + volume.getName() + "]状态不正常:" + volume.getStatus());
            }
            HostEntity host = this.allocateService.allocateHost(0, 0, 0, 0);
            StorageEntity targetStorage = storageMapper.selectById(targetVolume.getStorageId());
            VolumeMigrateRequest request = VolumeMigrateRequest.builder()
                    .sourceVolume(initVolume(storage, volume))
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
    public void onFinish(MigrateVolumeOperate param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
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
        if (volume != null && volume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.MIGRATE) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                GuestDiskEntity guestDisk = guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq(GuestDiskEntity.VOLUME_ID, volume.getVolumeId()));
                if (guestDisk != null && targetVolume != null) {
                    guestDisk.setVolumeId(targetVolume.getVolumeId());
                    guestDiskMapper.updateById(guestDisk);
                }
                //提交源磁盘的销毁任务
                volume.setStatus(cn.chenjun.cloud.management.util.Constant.VolumeStatus.DESTROY);
                volumeMapper.updateById(volume);
                this.taskService.addTask(DestroyVolumeOperate.builder().id(UUID.randomUUID().toString()).volumeId(volume.getVolumeId()).build());
            } else {
                volume.setStatus(cn.chenjun.cloud.management.util.Constant.VolumeStatus.READY);
                volumeMapper.updateById(volume);
            }
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(param.getSourceVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
        this.notifyService.publish(NotifyData.<Void>builder().id(param.getTargetVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.MIGRATE_VOLUME;
    }
}
