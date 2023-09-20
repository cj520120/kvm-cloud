package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.NotifyMessage;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeInfo;
import cn.chenjun.cloud.common.bean.VolumeResizeRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.operate.bean.ResizeVolumeOperate;
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
public class ResizeVolumeOperateImpl extends AbstractOperate<ResizeVolumeOperate, ResultUtil<VolumeInfo>> {

    public ResizeVolumeOperateImpl() {
        super(ResizeVolumeOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(ResizeVolumeOperate param) {
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        if (volume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.RESIZE) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if (storage.getStatus() != cn.chenjun.cloud.management.util.Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            HostEntity host = this.allocateService.allocateHost(0, 0, 0, 0);
            VolumeResizeRequest request = VolumeResizeRequest.builder()
                    .sourceStorage(storage.getName())
                    .sourceVolume(volume.getPath())
                    .size(param.getSize())
                    .build();
            this.asyncInvoker(host, param, Constant.Command.VOLUME_RESIZE, request);
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
    public void onFinish(ResizeVolumeOperate param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        if (volume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.RESIZE) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                volume.setCapacity(resultUtil.getData().getCapacity());
                volume.setAllocation(resultUtil.getData().getAllocation());
                volume.setType(resultUtil.getData().getType());
                volume.setPath(resultUtil.getData().getPath());
                volume.setBackingPath(resultUtil.getData().getBackingPath());
            }
            volume.setStatus(cn.chenjun.cloud.management.util.Constant.VolumeStatus.READY);
            volumeMapper.updateById(volume);
        }
        this.notifyService.publish(NotifyMessage.builder().id(param.getVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
    }
}
