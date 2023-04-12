package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.NotifyInfo;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeCloneRequest;
import cn.chenjun.cloud.common.bean.VolumeInfo;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.SnapshotVolumeEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.operate.bean.CreateVolumeSnapshotOperate;
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
public class CreateVolumeSnapshotOperateImpl extends AbstractOperate<CreateVolumeSnapshotOperate, ResultUtil<VolumeInfo>> {

    public CreateVolumeSnapshotOperateImpl() {
        super(CreateVolumeSnapshotOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(CreateVolumeSnapshotOperate param) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.CREATE_SNAPSHOT) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if (storage.getStatus() != cn.chenjun.cloud.management.util.Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            SnapshotVolumeEntity targetVolume = this.snapshotVolumeMapper.selectById(param.getSnapshotVolumeId());
            if (targetVolume.getStatus() != cn.chenjun.cloud.management.util.Constant.SnapshotStatus.CREATING) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "目标磁盘[" + volume.getName() + "]状态不正常:" + volume.getStatus());
            }
            HostEntity host = this.allocateService.allocateHost(0, 0, 0, 0);
            StorageEntity targetStorage = storageMapper.selectById(targetVolume.getStorageId());
            VolumeCloneRequest request = VolumeCloneRequest.builder()
                    .sourceStorage(storage.getName())
                    .sourceVolume(volume.getPath())
                    .targetStorage(targetStorage.getName())
                    .targetName(targetVolume.getVolumeName())
                    .targetVolume(targetVolume.getVolumePath())
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
    public void onFinish(CreateVolumeSnapshotOperate param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.CREATE_SNAPSHOT) {
            volume.setStatus(cn.chenjun.cloud.management.util.Constant.VolumeStatus.READY);
            volumeMapper.updateById(volume);
        }
        SnapshotVolumeEntity targetVolume = this.snapshotVolumeMapper.selectById(param.getSnapshotVolumeId());
        if (targetVolume.getStatus() == cn.chenjun.cloud.management.util.Constant.SnapshotStatus.CREATING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                targetVolume.setStatus(cn.chenjun.cloud.management.util.Constant.SnapshotStatus.READY);
                targetVolume.setAllocation(resultUtil.getData().getAllocation());
                targetVolume.setCapacity(resultUtil.getData().getCapacity());
                targetVolume.setType(resultUtil.getData().getType());
                targetVolume.setVolumePath(resultUtil.getData().getPath());
            } else {
                targetVolume.setStatus(cn.chenjun.cloud.management.util.Constant.SnapshotStatus.ERROR);
            }
            this.snapshotVolumeMapper.updateById(targetVolume);
        }

        this.notifyService.publish(NotifyInfo.builder().id(param.getSourceVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());

        this.notifyService.publish(NotifyInfo.builder().id(param.getSnapshotVolumeId()).type(Constant.NotifyType.UPDATE_SNAPSHOT).build());

    }
}
