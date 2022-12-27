package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.NotifyInfo;
import cn.chenjun.cloud.common.bean.OsDisk;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.operate.bean.ChangeGuestDiskOperate;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
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

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(ChangeGuestDiskOperate param) {
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        switch (volume.getStatus()) {
            case cn.chenjun.cloud.management.util.Constant.VolumeStatus.ATTACH_DISK:
            case cn.chenjun.cloud.management.util.Constant.VolumeStatus.DETACH_DISK:
                GuestEntity guest = guestMapper.selectById(param.getGuestId());
                if (guest.getHostId() > 0) {
                    HostEntity host = hostMapper.selectById(guest.getHostId());
                    OsDisk disk = OsDisk.builder().name(guest.getName()).deviceId(param.getDeviceId()).volume(volume.getPath()).volumeType(volume.getType()).build();
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
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        switch (volume.getStatus()) {
            case cn.chenjun.cloud.management.util.Constant.VolumeStatus.ATTACH_DISK:
            case cn.chenjun.cloud.management.util.Constant.VolumeStatus.DETACH_DISK:
                volume.setStatus(cn.chenjun.cloud.management.util.Constant.VolumeStatus.READY);
                volumeMapper.updateById(volume);
                break;
        }
        this.notifyService.publish(NotifyInfo.builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
        this.notifyService.publish(NotifyInfo.builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
    }
}