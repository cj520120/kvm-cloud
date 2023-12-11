package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.OsDisk;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.Storage;
import cn.chenjun.cloud.common.bean.Volume;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.operate.bean.ChangeGuestDiskOperate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * 更改磁盘挂载
 *
 * @author chenjun
 */
@Component
@Slf4j
public class ChangeGuestDiskOperateImpl extends AbstractOperate<ChangeGuestDiskOperate, ResultUtil<Void>> {


    @Override
    public void operate(ChangeGuestDiskOperate param) {
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        switch (volume.getStatus()) {
            case cn.chenjun.cloud.management.util.Constant.VolumeStatus.ATTACH_DISK:
            case cn.chenjun.cloud.management.util.Constant.VolumeStatus.DETACH_DISK:
                GuestEntity guest = guestMapper.selectById(param.getGuestId());
                if (guest.getHostId() > 0) {
                    HostEntity host = hostMapper.selectById(guest.getHostId());
                    StorageEntity storageEntity = this.storageMapper.selectById(volume.getStorageId());
                    if (storageEntity == null) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]磁盘[" + volume.getName() + "]所属存储池不存在");
                    }
                    if (storageEntity.getStatus() != cn.chenjun.cloud.management.util.Constant.StorageStatus.READY) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]磁盘[" + volume.getName() + "]所属存储池未就绪:" + storageEntity.getStatus());
                    }
                    Map<String, Object> storageParam = GsonBuilderUtil.create().fromJson(storageEntity.getParam(), new TypeToken<Map<String, Object>>() {
                    }.getType());
                    Storage storage = Storage.builder()
                            .name(storageEntity.getName())
                            .type(storageEntity.getType())
                            .param(storageParam)
                            .mountPath(storageEntity.getMountPath())
                            .build();
                    Volume diskVolume = Volume.builder().name(volume.getName()).type(volume.getType()).path(volume.getPath()).storage(storage).build();
                    OsDisk disk = OsDisk.builder().name(guest.getName()).deviceId(param.getDeviceId()).volume(diskVolume).build();
                    if (param.isAttach()) {

                        this.asyncInvoker(host, param, Constant.Command.GUEST_ATTACH_DISK, disk);
                    } else {
                        this.asyncInvoker(host, param, Constant.Command.GUEST_DETACH_DISK, disk);
                    }

                } else {
                    this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
                }
                break;
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "磁盘[" + volume.getName() + "]状态不正确:" + volume.getStatus());

        }

    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onFinish(ChangeGuestDiskOperate param, ResultUtil<Void> resultUtil) {
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        if (volume != null) {
            switch (volume.getStatus()) {
                case cn.chenjun.cloud.management.util.Constant.VolumeStatus.ATTACH_DISK:
                case cn.chenjun.cloud.management.util.Constant.VolumeStatus.DETACH_DISK:
                    volume.setStatus(cn.chenjun.cloud.management.util.Constant.VolumeStatus.READY);
                    volumeMapper.updateById(volume);
                    break;
                default:
                    break;
            }
        }
        this.eventService.publish(NotifyData.<Void>builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
        this.eventService.publish(NotifyData.<Void>builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.CHANGE_GUEST_DISK;
    }
}
