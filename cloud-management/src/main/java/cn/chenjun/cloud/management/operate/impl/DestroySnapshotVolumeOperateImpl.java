package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeDestroyRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.SnapshotVolumeEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.operate.bean.DestroySnapshotVolumeOperate;
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
public class DestroySnapshotVolumeOperateImpl extends AbstractOperate<DestroySnapshotVolumeOperate, ResultUtil<Void>> {


    @Override
    public void operate(DestroySnapshotVolumeOperate param) {
        SnapshotVolumeEntity volume = this.snapshotVolumeMapper.selectById(param.getSnapshotVolumeId());
        if (volume.getStatus() == cn.chenjun.cloud.management.util.Constant.SnapshotStatus.DESTROY) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if (storage.getStatus() != cn.chenjun.cloud.management.util.Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            HostEntity host = this.allocateService.allocateHost(0, 0, 0, 0);
            VolumeDestroyRequest request = VolumeDestroyRequest.builder()
                    .volume(initVolume(storage, volume))
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

    @Override
    public void onFinish(DestroySnapshotVolumeOperate param, ResultUtil<Void> resultUtil) {
        SnapshotVolumeEntity volume = this.snapshotVolumeMapper.selectById(param.getSnapshotVolumeId());
        if (volume != null && volume.getStatus() == cn.chenjun.cloud.management.util.Constant.SnapshotStatus.DESTROY) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                snapshotVolumeMapper.deleteById(param.getSnapshotVolumeId());
            } else {
                volume.setStatus(cn.chenjun.cloud.management.util.Constant.SnapshotStatus.ERROR);
                this.snapshotVolumeMapper.updateById(volume);
            }
        }

        this.notifyService.publish(NotifyData.<Void>builder().id(param.getSnapshotVolumeId()).type(Constant.NotifyType.UPDATE_SNAPSHOT).build());
    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.DESTROY_SNAPSHOT_VOLUME;
    }
}
