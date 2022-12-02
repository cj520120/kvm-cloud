package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.OsDisk;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.util.GsonBuilderUtil;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.data.entity.GuestDiskEntity;
import cn.roamblue.cloud.management.v2.data.entity.GuestEntity;
import cn.roamblue.cloud.management.v2.data.entity.HostEntity;
import cn.roamblue.cloud.management.v2.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.v2.data.mapper.GuestDiskMapper;
import cn.roamblue.cloud.management.v2.data.mapper.GuestMapper;
import cn.roamblue.cloud.management.v2.data.mapper.HostMapper;
import cn.roamblue.cloud.management.v2.data.mapper.VolumeMapper;
import cn.roamblue.cloud.management.v2.operate.OperateFactory;
import cn.roamblue.cloud.management.v2.operate.bean.ChangeGuestDiskOperate;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * 更改磁盘挂载
 *
 * @author chenjun
 */
public class ChangeGuestDiskOperateImpl extends AbstractOperate<ChangeGuestDiskOperate, ResultUtil<Void>> {

    public ChangeGuestDiskOperateImpl() {
        super(ChangeGuestDiskOperate.class);
    }

    @Override
    public void operate(ChangeGuestDiskOperate param) {
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestDiskMapper guestDiskMapper = SpringContextUtils.getBean(GuestDiskMapper.class);
        VolumeMapper volumeMapper = SpringContextUtils.getBean(VolumeMapper.class);
        GuestDiskEntity guestDisk = guestDiskMapper.selectById(param.getId());
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        switch (volume.getStatus()) {
            case cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.ATTACH_DISK:
            case cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.DETACH_DISK:
                volume.setStatus(cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.READY);
                volumeMapper.updateById(volume);
                if (!param.isAttach()) {
                    guestDiskMapper.deleteById(guestDisk.getId());
                }
                break;
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "磁盘[" + volume.getName() + "]状态不正确:" + volume.getStatus());

        }
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        switch (guest.getStatus()) {
            case cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.ATTACH_DISK:
            case cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.DETACH_DISK:
                if (guest.getHostId() > 0) {
                    HostEntity host = hostMapper.selectById(guest.getHostId());
                    OsDisk disk = OsDisk.builder().name(guest.getName()).deviceId(guestDisk.getDeviceId()).volume(volume.getTarget()).volumeType(volume.getType()).build();
                    if (param.isAttach()) {
                        this.asyncCall(host, param, Constant.Command.GUEST_ATTACH_DISK, disk);
                    } else {
                        this.asyncCall(host, param, Constant.Command.GUEST_DETACH_DISK, disk);
                    }
                }
                break;
            default:
                this.onSubmitCallback(param.getTaskId(), ResultUtil.<Void>builder().build());
                break;
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onCallback(String hostId, ChangeGuestDiskOperate param, ResultUtil<Void> resultUtil) {
        GuestDiskMapper guestDiskMapper = SpringContextUtils.getBean(GuestDiskMapper.class);
        VolumeMapper volumeMapper = SpringContextUtils.getBean(VolumeMapper.class);
        GuestDiskEntity guestDisk = guestDiskMapper.selectById(param.getId());
        VolumeEntity volume = volumeMapper.selectById(guestDisk.getVolumeId());
        switch (volume.getStatus()) {
            case cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.ATTACH_DISK:
            case cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.DETACH_DISK:
                volume.setStatus(cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.READY);
                volumeMapper.updateById(volume);
                if (!param.isAttach()) {
                    guestDiskMapper.deleteById(guestDisk.getId());
                }
                break;
        }
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(guestDisk.getGuestId());
        switch (guest.getStatus()) {
            case cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.ATTACH_DISK:
            case cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.DETACH_DISK:
                if (guest.getHostId() > 0) {
                    guest.setStatus(cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.RUNNING);
                } else {
                    guest.setStatus(cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.STOP);
                }
                guestMapper.updateById(guest);
                break;
        }
    }
}