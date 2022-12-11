package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.NotifyInfo;
import cn.roamblue.cloud.common.bean.OsDisk;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.GuestDiskEntity;
import cn.roamblue.cloud.management.data.entity.GuestEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.operate.bean.ChangeGuestDiskOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;

/**
 * 更改磁盘挂载
 *
 * @author chenjun
 */
@Component
@Slf4j
public class ChangeGuestDiskOperateImpl extends AbstractOperate<ChangeGuestDiskOperate, ResultUtil<Void>> {

    public ChangeGuestDiskOperateImpl() {
        super(ChangeGuestDiskOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY,write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(ChangeGuestDiskOperate param) {
        GuestDiskEntity guestDisk = guestDiskMapper.selectById(param.getGuestDiskId());
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        switch (volume.getStatus()) {
            case cn.roamblue.cloud.management.util.Constant.VolumeStatus.ATTACH_DISK:
            case cn.roamblue.cloud.management.util.Constant.VolumeStatus.DETACH_DISK:
                GuestEntity guest = guestMapper.selectById(param.getGuestId());
                if (guest.getHostId() > 0) {
                    HostEntity host = hostMapper.selectById(guest.getHostId());
                    OsDisk disk = OsDisk.builder().name(guest.getName()).deviceId(guestDisk.getDeviceId()).volume(volume.getPath()).volumeType(volume.getType()).build();
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
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(ChangeGuestDiskOperate param, ResultUtil<Void> resultUtil) {
        GuestDiskEntity guestDisk = guestDiskMapper.selectById(param.getGuestDiskId());
        VolumeEntity volume = volumeMapper.selectById(guestDisk.getVolumeId());
        switch (volume.getStatus()) {
            case cn.roamblue.cloud.management.util.Constant.VolumeStatus.ATTACH_DISK:
            case cn.roamblue.cloud.management.util.Constant.VolumeStatus.DETACH_DISK:
                volume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.READY);
                volumeMapper.updateById(volume);
                if (!param.isAttach()) {
                    guestDiskMapper.deleteById(guestDisk.getGuestDiskId());
                }
                break;
        }
        this.notifyService.publish(NotifyInfo.builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
        this.notifyService.publish(NotifyInfo.builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
    }
}