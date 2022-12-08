package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeCloneRequest;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.SnapshotVolumeEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.operate.bean.CreateVolumeSnapshotOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
public class CreateVolumeSnapshotOperateImpl extends AbstractOperate<CreateVolumeSnapshotOperate, ResultUtil<VolumeInfo>> {

    public CreateVolumeSnapshotOperateImpl() {
        super(CreateVolumeSnapshotOperate.class);
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(CreateVolumeSnapshotOperate param) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.CREATE_SNAPSHOT) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if (storage.getStatus() != cn.roamblue.cloud.management.util.Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            SnapshotVolumeEntity targetVolume = this.snapshotVolumeMapper.selectById(param.getSnapshotVolumeId());
            if (targetVolume.getStatus() != cn.roamblue.cloud.management.util.Constant.SnapshotStatus.CREATING) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "目标磁盘[" + volume.getName() + "]状态不正常:" + volume.getStatus());
            }
            List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<>());
            Collections.shuffle(hosts);
            HostEntity host = hosts.stream().filter(h -> Objects.equals(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE, h.getStatus())).findFirst().orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "没有可用的主机信息"));
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
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.CREATE_SNAPSHOT) {
            volume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.READY);
            volumeMapper.updateById(volume);
        }
        SnapshotVolumeEntity targetVolume = this.snapshotVolumeMapper.selectById(param.getSnapshotVolumeId());
        if (targetVolume.getStatus() == cn.roamblue.cloud.management.util.Constant.SnapshotStatus.CREATING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                targetVolume.setStatus(cn.roamblue.cloud.management.util.Constant.SnapshotStatus.READY);
                targetVolume.setAllocation(resultUtil.getData().getAllocation());
                targetVolume.setCapacity(resultUtil.getData().getCapacity());
            } else {
                targetVolume.setStatus(cn.roamblue.cloud.management.util.Constant.SnapshotStatus.ERROR);
            }
            this.snapshotVolumeMapper.updateById(targetVolume);
        }
    }
}
