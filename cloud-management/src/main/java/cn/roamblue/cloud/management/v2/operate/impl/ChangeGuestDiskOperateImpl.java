package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.OsDisk;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.data.entity.GuestDiskEntity;
import cn.roamblue.cloud.management.v2.data.entity.GuestEntity;
import cn.roamblue.cloud.management.v2.data.entity.HostEntity;
import cn.roamblue.cloud.management.v2.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.v2.data.mapper.GuestDiskMapper;
import cn.roamblue.cloud.management.v2.data.mapper.GuestMapper;
import cn.roamblue.cloud.management.v2.data.mapper.HostMapper;
import cn.roamblue.cloud.management.v2.data.mapper.VolumeMapper;
import cn.roamblue.cloud.management.v2.operate.bean.ChangeGuestDiskOperate;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class ChangeGuestDiskOperateImpl extends AbstractOperate<ChangeGuestDiskOperate, ResultUtil<Void>> {

    protected ChangeGuestDiskOperateImpl() {
        super(ChangeGuestDiskOperate.class);
    }

    @Override
    public void operate(ChangeGuestDiskOperate param) {
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestDiskMapper guestDiskMapper = SpringContextUtils.getBean(GuestDiskMapper.class);
        VolumeMapper volumeMapper = SpringContextUtils.getBean(VolumeMapper.class);
        GuestDiskEntity guestDisk = guestDiskMapper.selectById(param.getId());
        GuestEntity guest = guestMapper.selectById(guestDisk.getGuestId());
        HostEntity host = hostMapper.selectById(guest.getHostId());
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        OsDisk disk = OsDisk.builder().name(guest.getName()).deviceId(guestDisk.getDeviceId()).volume(volume.getTarget()).volumeType(volume.getType()).build();
        if (param.isAttach()) {
            this.asyncCall(host, param, Constant.Command.GUEST_ATTACH_DISK, disk);
        } else {
            this.asyncCall(host, param, Constant.Command.GUEST_DETACH_DISK, disk);
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onCallback(String hostId, ChangeGuestDiskOperate param, ResultUtil<Void> resultUtil) {
        //无论是否成功，将强制变更状态，等待下次重启后生效
        GuestDiskMapper guestDiskMapper = SpringContextUtils.getBean(GuestDiskMapper.class);
        VolumeMapper volumeMapper = SpringContextUtils.getBean(VolumeMapper.class);
        GuestDiskEntity guestDisk = guestDiskMapper.selectById(param.getId());
        VolumeEntity volume = volumeMapper.selectById(guestDisk.getVolumeId());
        volume.setStatus(cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.READY);
        volumeMapper.updateById(volume);
        if (!param.isAttach()) {
            guestDiskMapper.deleteById(guestDisk.getId());
        }
    }
}