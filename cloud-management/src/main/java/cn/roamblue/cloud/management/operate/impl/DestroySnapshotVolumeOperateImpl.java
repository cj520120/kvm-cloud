package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.NotifyInfo;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeDestroyRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.SnapshotVolumeEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.operate.bean.DestroySnapshotVolumeOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;

/**
 * 销毁磁盘
 *
 * @author chenjun
 */
@Component
@Slf4j
public class DestroySnapshotVolumeOperateImpl extends AbstractOperate<DestroySnapshotVolumeOperate, ResultUtil<Void>> {

    public DestroySnapshotVolumeOperateImpl() {
        super(DestroySnapshotVolumeOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(DestroySnapshotVolumeOperate param) {
        SnapshotVolumeEntity volume = this.snapshotVolumeMapper.selectById(param.getSnapshotVolumeId());
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.SnapshotStatus.DESTROY) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if (storage.getStatus() != cn.roamblue.cloud.management.util.Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            HostEntity host = this.allocateService.allocateHost(0, 0, 0, 0);
            VolumeDestroyRequest request = VolumeDestroyRequest.builder()
                    .sourceStorage(storage.getName())
                    .sourceVolume(volume.getVolumePath())
                    .sourceType(volume.getType())
                    .build();
            this.asyncInvoker(host, param, Constant.Command.VOLUME_DESTROY, request);
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "磁盘[" + volume.getName() + "]状态不正确:" + volume.getStatus());
        }


    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(DestroySnapshotVolumeOperate param, ResultUtil<Void> resultUtil) {
        SnapshotVolumeEntity volume = this.snapshotVolumeMapper.selectById(param.getSnapshotVolumeId());
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.SnapshotStatus.DESTROY) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                snapshotVolumeMapper.deleteById(param.getSnapshotVolumeId());
            } else {
                volume.setStatus(cn.roamblue.cloud.management.util.Constant.SnapshotStatus.ERROR);
                this.snapshotVolumeMapper.updateById(volume);
            }
        }

        this.notifyService.publish(NotifyInfo.builder().id(param.getSnapshotVolumeId()).type(Constant.NotifyType.UPDATE_SNAPSHOT).build());
    }
}
