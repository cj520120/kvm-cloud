package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.bean.VolumeMigrateRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.GuestDiskEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.operate.bean.DestroyVolumeOperate;
import cn.roamblue.cloud.management.operate.bean.MigrateVolumeOperate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    @Override
    public void operate(MigrateVolumeOperate param) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.MIGRATE) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if(storage.getStatus()!= cn.roamblue.cloud.management.util.Constant.StorageStatus.READY){
                throw new CodeException(ErrorCode.STORAGE_NOT_READY,"存储池未就绪");
            }
            List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<HostEntity>().eq("cluster_id", volume.getClusterId()));
            Collections.shuffle(hosts);
            HostEntity host = hosts.stream().filter(h -> Objects.equals(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE, h.getStatus())).findFirst().orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "没有可用的主机信息"));
            StorageEntity targetStorage = storageMapper.selectById(param.getTargetStorageId());
            VolumeMigrateRequest request = VolumeMigrateRequest.builder()
                    .sourceStorage(storage.getName())
                    .sourceVolume(volume.getPath())
                    .targetStorage(targetStorage.getName())
                    .targetName(param.getTargetName())
                    .targetVolume(param.getTargetPath())
                    .targetType(param.getTargetType())

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

    @Override
    public void onFinish(MigrateVolumeOperate param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.MIGRATE) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                VolumeEntity targetVolume = VolumeEntity.builder()
                        .path(param.getTargetPath())
                        .clusterId(volume.getClusterId())
                        .storageId(param.getTargetStorageId())
                        .type(param.getTargetType())
                        .path(param.getTargetPath())
                        .name(param.getTargetName())
                        .templateId(volume.getTemplateId())
                        .allocation(resultUtil.getData().getAllocation())
                        .capacity(resultUtil.getData().getCapacity())
                        .status(cn.roamblue.cloud.management.util.Constant.VolumeStatus.READY)
                        .build();
                volumeMapper.insert(targetVolume);
                GuestDiskEntity guestDisk = guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq("volume_id", volume.getVolumeId()));
                if (guestDisk != null) {
                    guestDisk.setVolumeId(targetVolume.getVolumeId());
                    guestDiskMapper.updateById(guestDisk);
                }
                //提交源磁盘的销毁任务
                volume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.DESTROY);
                volumeMapper.updateById(volume);
                this.operateTask.addTask(DestroyVolumeOperate.builder().taskId(UUID.randomUUID().toString()).volumeId(volume.getVolumeId()).build());


            }else {
                volume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.READY);
                volumeMapper.updateById(volume);
            }
        }
    }
}
