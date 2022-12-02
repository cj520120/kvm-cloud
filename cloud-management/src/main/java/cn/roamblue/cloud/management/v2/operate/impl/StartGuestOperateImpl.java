package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.*;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.data.entity.*;
import cn.roamblue.cloud.management.v2.data.mapper.*;
import cn.roamblue.cloud.management.v2.operate.bean.StartGuestOperate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StartGuestOperateImpl extends AbstractOperate<StartGuestOperate, ResultUtil<GuestInfo>> {

    protected StartGuestOperateImpl() {
        super(StartGuestOperate.class);
    }

    @Override
    public void operate(StartGuestOperate param) {
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        NetworkMapper networkMapper = SpringContextUtils.getBean(NetworkMapper.class);
        VolumeMapper volumeMapper = SpringContextUtils.getBean(VolumeMapper.class);
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestDiskMapper guestDiskMapper = SpringContextUtils.getBean(GuestDiskMapper.class);
        GuestNetworkMapper guestNetworkMapper = SpringContextUtils.getBean(GuestNetworkMapper.class);
        GuestEntity guest = guestMapper.selectById(param.getId());
        List<GuestDiskEntity> guestDiskEntityList = guestDiskMapper.selectList(new QueryWrapper<GuestDiskEntity>().eq("guest_id", guest.getId()));
        List<GuestNetworkEntity> guestNetworkEntityList = guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", guest.getId()));
        List<OsDisk> disks = new ArrayList<>();
        for (GuestDiskEntity entity : guestDiskEntityList) {
            VolumeEntity volume = volumeMapper.selectById(entity.getVolumeId());
            OsDisk disk = OsDisk.builder().name(guest.getName()).deviceId(entity.getDeviceId()).volume(volume.getTarget()).volumeType(volume.getType()).build();
            disks.add(disk);
        }
        Collections.sort(disks, Comparator.comparingInt(OsDisk::getDeviceId));
        List<OsNic> networkInterfaces = new ArrayList<>();
        for (GuestNetworkEntity entity : guestNetworkEntityList) {
            NetworkEntity network = networkMapper.selectById(entity.getNetworkId());
            OsNic nic = OsNic.builder()
                    .mac(entity.getMac())
                    .driveType(entity.getDrive())
                    .name(guest.getName())
                    .deviceId(entity.getDeviceId())
                    .bridgeName(network.getBridge())
                    .build();
            networkInterfaces.add(nic);
        }
        HostEntity host = hostMapper.selectById(guest.getHostId());
        GuestStartRequest request = GuestStartRequest.builder()
                .emulator(host.getEmulator())
                .name(guest.getName())
                .description(guest.getDescription())
                .bus(guest.getBus())
                .osCpu(OsCpu.builder().number(guest.getCpu()).build())
                .osMemory(OsMemory.builder().memory(guest.getMemory()).build())
                .osCdRoom(OsCdRoom.builder()
                        .path(guest.getCdRoom())
                        .build())
                .osDisks(disks)
                .networkInterfaces(networkInterfaces)
                .vncPassword(guest.getVncPassword())
                .build();
        this.asyncCall(host, param, Constant.Command.GUEST_START, request);

    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<GuestInfo>>() {
        }.getType();
    }

    @Override
    public void onCallback(String hostId, StartGuestOperate param, ResultUtil<GuestInfo> resultUtil) {
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(param.getId());
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            guest.setStatus(cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.RUNNING);
        } else {
            guest.setStatus(cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.STOP);
        }
        guestMapper.updateById(guest);
    }
}