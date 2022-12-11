package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.*;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.*;
import cn.roamblue.cloud.management.operate.bean.StartGuestOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 启动虚拟机
 *
 * @author chenjun
 */
@Component
@Slf4j
public class StartGuestOperateImpl<T extends StartGuestOperate> extends AbstractOperate<T, ResultUtil<GuestInfo>> {

    public StartGuestOperateImpl() {
        super((Class<T>) StartGuestOperate.class);
    }

    public StartGuestOperateImpl(Class<T> tClass) {
        super(tClass);
    }


    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY,write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(T param) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest.getStatus() != cn.roamblue.cloud.management.util.Constant.GuestStatus.STARTING) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getName() + "]状态不正确:" + guest.getStatus());
        }

        HostEntity host =this.allocateService.allocateHost(guest.getLastHostId(),param.getHostId(),guest.getCpu(), guest.getMemory());
        List<OsDisk> disks = getGuestDisk(guest);
        List<OsNic> networkInterfaces = getGuestNetwork(guest);
        OsCdRoom cdRoom = getGuestCdRoom(guest);
        GuestVncEntity guestVncEntity = this.guestVncMapper.selectById(guest.getGuestId());
        if (guestVncEntity == null) {
            guestVncEntity = GuestVncEntity.builder()
                    .guestId(param.getGuestId())
                    .port(0)
                    .password(RandomStringUtils.randomAlphanumeric(8))
                    .token(RandomStringUtils.randomAlphanumeric(16))
                    .build();
            this.guestVncMapper.insert(guestVncEntity);
        }
        guest.setHostId(host.getHostId());
        this.guestMapper.updateById(guest);
        this.allocateService.initHostAllocate();
        GuestStartRequest request = GuestStartRequest.builder()
                .emulator(host.getEmulator())
                .name(guest.getName())
                .description(guest.getDescription())
                .bus(guest.getBusType())
                .osCpu(OsCpu.builder().number(guest.getCpu()).build())
                .osMemory(OsMemory.builder().memory(guest.getMemory()).build())
                .osCdRoom(cdRoom)
                .osDisks(disks)
                .networkInterfaces(networkInterfaces)
                .vncPassword(guestVncEntity.getPassword())
                .build();
        this.asyncInvoker(host, param, Constant.Command.GUEST_START, request);

    }


    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<GuestInfo>>() {
        }.getType();
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(T param, ResultUtil<GuestInfo> resultUtil) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest.getStatus() == cn.roamblue.cloud.management.util.Constant.GuestStatus.STARTING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                guest.setStatus(cn.roamblue.cloud.management.util.Constant.GuestStatus.RUNNING);
                GuestInfo guestInfo = resultUtil.getData();
                GuestVncEntity guestVncEntity = this.guestVncMapper.selectById(guest.getGuestId());
                if (guestVncEntity == null) {
                    guestVncEntity = GuestVncEntity.builder()
                            .guestId(guestVncEntity.getGuestId())
                            .port(guestInfo.getVnc())
                            .password(guestInfo.getPassword())
                            .token(RandomStringUtils.randomAlphanumeric(16))
                            .build();
                    this.guestVncMapper.insert(guestVncEntity);
                } else {
                    guestVncEntity.setPort(guestInfo.getVnc());
                    this.guestVncMapper.updateById(guestVncEntity);
                }
            } else {
                guest.setHostId(0);
                guest.setStatus(cn.roamblue.cloud.management.util.Constant.GuestStatus.STOP);
            }
            this.guestMapper.updateById(guest);
            this.allocateService.initHostAllocate();
        }
        this.notifyService.publish(NotifyInfo.builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
    }
    protected List<OsDisk> getGuestDisk(GuestEntity guest) {
        List<GuestDiskEntity> guestDiskEntityList = guestDiskMapper.selectList(new QueryWrapper<GuestDiskEntity>().eq("guest_id", guest.getGuestId()));
        List<OsDisk> disks = new ArrayList<>();
        for (GuestDiskEntity entity : guestDiskEntityList) {
            VolumeEntity volume = volumeMapper.selectById(entity.getVolumeId());
            if (volume.getStatus() != cn.roamblue.cloud.management.util.Constant.VolumeStatus.READY) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]磁盘[" + volume.getName() + "]未就绪:" + volume.getStatus());
            }
            OsDisk disk = OsDisk.builder().name(guest.getName()).deviceId(entity.getDeviceId()).volume(volume.getPath()).volumeType(volume.getType()).build();
            disks.add(disk);
        }
        Collections.sort(disks, Comparator.comparingInt(OsDisk::getDeviceId));
        return disks;
    }

    protected List<OsNic> getGuestNetwork(GuestEntity guest) {
        List<GuestNetworkEntity> guestNetworkEntityList = guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", guest.getGuestId()));
        List<OsNic> networkInterfaces = new ArrayList<>();
        for (GuestNetworkEntity entity : guestNetworkEntityList) {
            NetworkEntity network = networkMapper.selectById(entity.getNetworkId());
            if (network.getStatus() != cn.roamblue.cloud.management.util.Constant.NetworkStatus.READY) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getName() + "]网络[" + network.getName() + "]未就绪:" + network.getStatus());
            }
            if (network.getBasicNetworkId() > 0) {
                NetworkEntity parentNetwork = networkMapper.selectById(entity.getNetworkId());
                if (parentNetwork.getStatus() != cn.roamblue.cloud.management.util.Constant.NetworkStatus.READY) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getName() + "]网络[" + parentNetwork.getName() + "]未就绪:" + network.getStatus());
                }
            }
            OsNic nic = OsNic.builder()
                    .mac(entity.getMac())
                    .driveType(entity.getDriveType())
                    .name(guest.getName())
                    .deviceId(entity.getDeviceId())
                    .bridgeName(network.getBridge())
                    .build();
            networkInterfaces.add(nic);
        }
        return networkInterfaces;
    }

    protected OsCdRoom getGuestCdRoom(GuestEntity guest) {
        OsCdRoom cdRoom = OsCdRoom.builder().build();
        if (guest.getCdRoom() > 0) {
            List<TemplateVolumeEntity> templateVolumeList = templateVolumeMapper.selectList(new QueryWrapper<TemplateVolumeEntity>().eq("template_id", guest.getCdRoom()));
            Collections.shuffle(templateVolumeList);
            if (templateVolumeList.size() > 0) {
                TemplateVolumeEntity templateVolume = templateVolumeList.get(0);
                cdRoom.setPath(templateVolume.getPath());
            } else {
                throw new CodeException(ErrorCode.SERVER_ERROR, "光盘镜像未就绪");
            }
        }
        return cdRoom;
    }

}