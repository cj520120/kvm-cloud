package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.*;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.*;
import cn.roamblue.cloud.management.operate.bean.CreateVolumeOperate;
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
public class CreateVolumeOperateImpl<T extends CreateVolumeOperate> extends AbstractOperate<T, ResultUtil<VolumeInfo>> {

    public CreateVolumeOperateImpl() {
        super((Class<T>) CreateVolumeOperate.class);
    }

    public CreateVolumeOperateImpl(Class<T> tClass) {
        super(tClass);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(T param) {
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.CREATING) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if (storage.getStatus() != cn.roamblue.cloud.management.util.Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            HostEntity host = this.allocateService.allocateHost(0, 0, 0, 0);
            if (param.getTemplateId() > 0) {
                List<TemplateVolumeEntity> templateVolumeList = templateVolumeMapper.selectList(new QueryWrapper<TemplateVolumeEntity>().eq("template_id", param.getTemplateId()));
                Collections.shuffle(templateVolumeList);
                TemplateVolumeEntity templateVolume = templateVolumeList.stream().filter(t -> Objects.equals(t.getStatus(), cn.roamblue.cloud.management.util.Constant.TemplateStatus.READY)).findFirst().orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "当前模版未就绪"));
                StorageEntity parentStorage = storageMapper.selectById(templateVolume.getStorageId());
                VolumeCreateRequest request = VolumeCreateRequest.builder()
                        .parentStorage(parentStorage.getName())
                        .parentType(templateVolume.getType())
                        .parentVolume(templateVolume.getPath())
                        .targetStorage(storage.getName())
                        .targetVolume(volume.getPath())
                        .targetName(volume.getName())
                        .targetType(volume.getType())
                        .targetSize(volume.getCapacity())
                        .build();
                this.asyncInvoker(host, param, Constant.Command.VOLUME_CREATE, request);

            } else if (param.getSnapshotVolumeId() > 0) {
                SnapshotVolumeEntity snapshotVolume = snapshotVolumeMapper.selectById(param.getSnapshotVolumeId());
                if (snapshotVolume.getStatus() != cn.roamblue.cloud.management.util.Constant.SnapshotStatus.READY) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前快照未就绪");
                }
                StorageEntity parentStorage = storageMapper.selectById(snapshotVolume.getStorageId());
                VolumeCloneRequest request = VolumeCloneRequest.builder()
                        .sourceStorage(parentStorage.getName())
                        .sourceVolume(snapshotVolume.getVolumePath())
                        .targetStorage(storage.getName())
                        .targetVolume(volume.getPath())
                        .targetName(volume.getName())
                        .targetType(volume.getType())

                        .build();
                this.asyncInvoker(host, param, Constant.Command.VOLUME_CLONE, request);
            } else {
                VolumeCreateRequest request = VolumeCreateRequest.builder()
                        .targetStorage(storage.getName())
                        .targetVolume(volume.getPath())
                        .targetName(volume.getName())
                        .targetType(volume.getType())
                        .targetSize(volume.getCapacity())
                        .build();
                this.asyncInvoker(host, param, Constant.Command.VOLUME_CREATE, request);
            }
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
    public void onFinish(T param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.CREATING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                volume.setTemplateId(param.getTemplateId());
                volume.setAllocation(resultUtil.getData().getAllocation());
                volume.setCapacity(resultUtil.getData().getCapacity());
                volume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.READY);
            } else {
                volume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.ERROR);
            }
            volumeMapper.updateById(volume);
        }

        this.notifyService.publish(NotifyInfo.builder().id(param.getVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());

    }
}
