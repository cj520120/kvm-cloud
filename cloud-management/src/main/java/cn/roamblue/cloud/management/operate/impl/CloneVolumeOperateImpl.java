package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.NotifyInfo;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeCloneRequest;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.operate.bean.CloneVolumeOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;

/**
 * 创建磁盘
 *
 * @author chenjun
 */
@Component
@Slf4j
public class CloneVolumeOperateImpl extends AbstractOperate<CloneVolumeOperate, ResultUtil<VolumeInfo>> {

    public CloneVolumeOperateImpl() {
        super(CloneVolumeOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY,write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(CloneVolumeOperate param) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.CLONE) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if(storage.getStatus()!= cn.roamblue.cloud.management.util.Constant.StorageStatus.READY){
                throw new CodeException(ErrorCode.STORAGE_NOT_READY,"存储池未就绪");
            }
            VolumeEntity cloneVolume=volumeMapper.selectById(param.getTargetVolumeId());
            if(cloneVolume.getStatus()!= cn.roamblue.cloud.management.util.Constant.VolumeStatus.CREATING){
                throw new CodeException(ErrorCode.SERVER_ERROR, "目标磁盘[" + volume.getName() + "]状态不正常:" + volume.getStatus());
            }
            HostEntity host = this.allocateService.allocateHost(0, 0, 0, 0);
            StorageEntity targetStorage = storageMapper.selectById(cloneVolume.getStorageId());
            VolumeCloneRequest request = VolumeCloneRequest.builder()
                    .sourceStorage(storage.getName())
                    .sourceVolume(volume.getPath())
                    .targetStorage(targetStorage.getName())
                    .targetName(cloneVolume.getName())
                    .targetVolume(cloneVolume.getPath())
                    .targetType(cloneVolume.getType())

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
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(CloneVolumeOperate param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.CLONE) {
            volume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.READY);
            volumeMapper.updateById(volume);
        }
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
        this.notifyService.publish(NotifyInfo.builder().id(param.getSourceVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
        this.notifyService.publish(NotifyInfo.builder().id(param.getTargetVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
    }
}
