package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeDestroyRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.BootstrapType;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyVolumeOperate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * 销毁磁盘
 *
 * @author chenjun
 */
@Component
@Slf4j
public class DestroyVolumeOperateImpl extends AbstractOperate<DestroyVolumeOperate, ResultUtil<Void>> {


    @Override
    public void operate(DestroyVolumeOperate param) {
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        if (volume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.DESTROY) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if (storage.getStatus() != cn.chenjun.cloud.management.util.Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            HostEntity host = this.allocateService.allocateHost(0, BootstrapType.BIOS, 0, 0, 0);
            VolumeDestroyRequest request = VolumeDestroyRequest.builder()
                    .sourceStorage(storage.getName())
                    .sourceName(volume.getName())
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
    public void onFinish(DestroyVolumeOperate param, ResultUtil<Void> resultUtil) {
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        if (volume != null && volume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.DESTROY) {
            volumeMapper.deleteById(param.getVolumeId());
        }

        this.eventService.publish(NotifyData.<Void>builder().id(param.getVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.DESTROY_VOLUME;
    }
}
