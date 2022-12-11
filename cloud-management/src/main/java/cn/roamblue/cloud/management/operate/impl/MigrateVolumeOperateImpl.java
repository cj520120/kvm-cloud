package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.NotifyInfo;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.bean.VolumeMigrateRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.GuestDiskEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.operate.bean.DestroyVolumeOperate;
import cn.roamblue.cloud.management.operate.bean.MigrateVolumeOperate;
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
import java.util.UUID;

/**
 * 创建磁盘
 *
 * @author chenjun
 */
@Component
@Slf4j
public class MigrateVolumeOperateImpl extends AbstractOperate<MigrateVolumeOperate, ResultUtil<VolumeInfo>> {

    public MigrateVolumeOperateImpl() {
        super(MigrateVolumeOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY,write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(MigrateVolumeOperate param) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.MIGRATE) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if(storage.getStatus()!= cn.roamblue.cloud.management.util.Constant.StorageStatus.READY){
                throw new CodeException(ErrorCode.STORAGE_NOT_READY,"存储池未就绪");
            }
            VolumeEntity targetVolume=volumeMapper.selectById(param.getTargetVolumeId());
            if(targetVolume.getStatus()!= cn.roamblue.cloud.management.util.Constant.VolumeStatus.CREATING){
                throw new CodeException(ErrorCode.SERVER_ERROR, "目标磁盘[" + volume.getName() + "]状态不正常:" + volume.getStatus());
            }
            List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<>());
            Collections.shuffle(hosts);
            HostEntity host = hosts.stream().filter(h -> Objects.equals(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE, h.getStatus())).findFirst().orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "没有可用的主机信息"));
            StorageEntity targetStorage = storageMapper.selectById(targetVolume.getStorageId());
            VolumeMigrateRequest request = VolumeMigrateRequest.builder()
                    .sourceStorage(storage.getName())
                    .sourceVolume(volume.getPath())
                    .targetStorage(targetStorage.getName())
                    .targetName(targetVolume.getName())
                    .targetVolume(targetVolume.getPath())
                    .targetType(targetVolume.getType())

                    .build();

            this.asyncInvoker(host, param, Constant.Command.VOLUME_CLONE, request);
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "磁盘[" + volume.getName() + "]状态不正常:" + volume.getStatus());
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
    public void onFinish(MigrateVolumeOperate param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());

        VolumeEntity targetVolume=volumeMapper.selectById(param.getTargetVolumeId());
        if(targetVolume.getStatus()== cn.roamblue.cloud.management.util.Constant.VolumeStatus.CREATING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                targetVolume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.READY);
                targetVolume.setAllocation(resultUtil.getData().getAllocation());
                targetVolume.setCapacity(resultUtil.getData().getCapacity());
            } else {
                targetVolume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.ERROR);
            }
            volumeMapper.updateById(targetVolume);
        }
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.MIGRATE) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                volumeMapper.insert(targetVolume);
                GuestDiskEntity guestDisk = guestDiskMapper.selectOne(new QueryWrapper<>());
                if (guestDisk != null) {
                    guestDisk.setVolumeId(targetVolume.getVolumeId());
                    guestDiskMapper.updateById(guestDisk);
                }
                //提交源磁盘的销毁任务
                volume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.DESTROY);
                volumeMapper.updateById(volume);
                this.operateTask.addTask(DestroyVolumeOperate.builder().taskId(UUID.randomUUID().toString()).volumeId(volume.getVolumeId()).build());
            }
        }
        this.notifyService.publish(NotifyInfo.builder().id(param.getSourceVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
        this.notifyService.publish(NotifyInfo.builder().id(param.getTargetVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
    }
}
