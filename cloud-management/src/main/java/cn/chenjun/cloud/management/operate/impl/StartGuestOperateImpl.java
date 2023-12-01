package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.operate.bean.StartGuestOperate;
import cn.chenjun.cloud.management.servcie.VncService;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;

/**
 * 启动虚拟机
 *
 * @author chenjun
 */
@Component
@Slf4j
public class StartGuestOperateImpl<T extends StartGuestOperate> extends AbstractOperate<T, ResultUtil<GuestInfo>> {


    @Autowired
    private VncService vncService;

    public StartGuestOperateImpl() {
        super((Class<T>) StartGuestOperate.class);
    }

    public StartGuestOperateImpl(Class<T> tClass) {
        super(tClass);
    }


    @Override
    public void operate(T param) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest.getStatus() != cn.chenjun.cloud.management.util.Constant.GuestStatus.STARTING) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getName() + "]状态不正确:" + guest.getStatus());
        }

        HostEntity host = this.allocateService.allocateHost(guest.getLastHostId(), param.getHostId(), guest.getCpu(), guest.getMemory());
        List<OsDisk> disks = getGuestDisk(guest);
        List<OsNic> networkInterfaces = getGuestNetwork(guest);
        OsCdRoom cdRoom = getGuestCdRoom(guest);
        GuestVncEntity guestVncEntity = this.vncService.getGuestVnc(param.getGuestId());
        guest.setHostId(host.getHostId());
        guest.setLastStartTime(new Date());
        this.guestMapper.updateById(guest);
        this.allocateService.initHostAllocate();
        SchemeEntity scheme = this.schemeMapper.selectById(guest.getSchemeId());
        OsCpu cpu = OsCpu.builder().number(guest.getCpu()).share(guest.getSpeed()).build();
        if (scheme != null) {
            cpu.setCore(scheme.getCores());
            cpu.setThread(scheme.getThreads());
            cpu.setSocket(scheme.getSockets());
        }
        GuestStartRequest request = GuestStartRequest.builder()
                .emulator(host.getEmulator())
                .name(guest.getName())
                .description(guest.getDescription())
                .bus(guest.getBusType())
                .osCpu(cpu)
                .osMemory(OsMemory.builder().memory(guest.getMemory()).build())
                .osCdRoom(cdRoom)
                .osDisks(disks)
                .networkInterfaces(networkInterfaces)
                .vncPassword(guestVncEntity.getPassword())
                .qmaRequest(this.getStartQmaRequest(guest))
                .build();
        this.asyncInvoker(host, param, Constant.Command.GUEST_START, request);

    }


    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<GuestInfo>>() {
        }.getType();
    }

    @Override
    public void onFinish(T param, ResultUtil<GuestInfo> resultUtil) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest != null && guest.getStatus() == cn.chenjun.cloud.management.util.Constant.GuestStatus.STARTING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                guest.setStatus(cn.chenjun.cloud.management.util.Constant.GuestStatus.RUNNING);
                //写入系统vnc
                GuestInfo guestInfo = resultUtil.getData();
                this.vncService.updateVncPort(guest.getNetworkId(), param.getGuestId(), guestInfo.getVnc());
            } else {
                guest.setHostId(0);
                guest.setStatus(cn.chenjun.cloud.management.util.Constant.GuestStatus.STOP);
            }
            this.guestMapper.updateById(guest);
            this.allocateService.initHostAllocate();
        }
        this.eventService.publish(NotifyData.<Void>builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());

    }

    protected List<OsDisk> getGuestDisk(GuestEntity guest) {
        List<GuestDiskEntity> guestDiskEntityList = guestDiskMapper.selectList(new QueryWrapper<GuestDiskEntity>().eq("guest_id", guest.getGuestId()));
        List<OsDisk> disks = new ArrayList<>();
        Map<Integer, Storage> storageMap = Maps.newHashMap();
        for (GuestDiskEntity entity : guestDiskEntityList) {
            VolumeEntity volume = volumeMapper.selectById(entity.getVolumeId());
            if (volume.getStatus() != cn.chenjun.cloud.management.util.Constant.VolumeStatus.READY) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]磁盘[" + volume.getName() + "]未就绪:" + volume.getStatus());
            }
            Storage storage = storageMap.computeIfAbsent(volume.getStorageId(), storageId -> {
                StorageEntity storageEntity = this.storageMapper.selectById(storageId);
                if (storageEntity == null) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]磁盘[" + volume.getName() + "]所属存储池不存在");
                }
                if (storageEntity.getStatus() != cn.chenjun.cloud.management.util.Constant.StorageStatus.READY) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]磁盘[" + volume.getName() + "]所属存储池未就绪:" + storageEntity.getStatus());
                }
                Map<String, Object> storageParam = GsonBuilderUtil.create().fromJson(storageEntity.getParam(), new TypeToken<Map<String, Object>>() {
                }.getType());
                return Storage.builder()
                        .name(storageEntity.getName())
                        .type(storageEntity.getType())
                        .param(storageParam)
                        .mountPath(storageEntity.getMountPath())
                        .build();
            });
            Volume diskVolume = Volume.builder().name(volume.getName()).type(volume.getType()).path(volume.getPath()).storage(storage).build();

            OsDisk disk = OsDisk.builder().name(guest.getName()).volume(diskVolume).deviceId(entity.getDeviceId()).build();
            disks.add(disk);
        }
        disks.sort(Comparator.comparingInt(OsDisk::getDeviceId));
        return disks;
    }


    protected OsCdRoom getGuestCdRoom(GuestEntity guest) {
        OsCdRoom cdRoom = OsCdRoom.builder().build();
        if (guest.getCdRoom() > 0) {
            List<TemplateVolumeEntity> templateVolumeList = templateVolumeMapper.selectList(new QueryWrapper<TemplateVolumeEntity>().eq("template_id", guest.getCdRoom()));
            Collections.shuffle(templateVolumeList);
            if (!templateVolumeList.isEmpty()) {
                TemplateVolumeEntity templateVolume = templateVolumeList.get(0);
                if (templateVolume != null) {
                    StorageEntity storageEntity = this.storageMapper.selectById(templateVolume.getStorageId());
                    if (storageEntity == null) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]光盘[" + templateVolume.getName() + "]所属存储池不存在");
                    }
                    if (storageEntity.getStatus() != cn.chenjun.cloud.management.util.Constant.StorageStatus.READY) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]光盘[" + templateVolume.getName() + "]所属存储池未就绪:" + storageEntity.getStatus());
                    }
                    Map<String, Object> storageParam = GsonBuilderUtil.create().fromJson(storageEntity.getParam(), new TypeToken<Map<String, Object>>() {
                    }.getType());
                    Storage storage = Storage.builder()
                            .name(storageEntity.getName())
                            .type(storageEntity.getType())
                            .param(storageParam)
                            .mountPath(storageEntity.getMountPath())
                            .build();
                    Volume cdVolume = Volume.builder().name(templateVolume.getName()).type(templateVolume.getType()).path(templateVolume.getPath()).storage(storage).build();
                    cdRoom.setVolume(cdVolume);
                }
            } else {
                throw new CodeException(ErrorCode.SERVER_ERROR, "光盘镜像未就绪");
            }
        }
        return cdRoom;
    }

    protected GuestQmaRequest getStartQmaRequest(GuestEntity guest) {

        return null;
    }

    protected List<OsNic> getGuestNetwork(GuestEntity guest) {
        List<GuestNetworkEntity> guestNetworkEntityList = guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", guest.getGuestId()));
        guestNetworkEntityList.sort(Comparator.comparingInt(GuestNetworkEntity::getDeviceId));
        List<OsNic> networkInterfaces = new ArrayList<>();
        int baseDeviceId = 0;
        for (GuestNetworkEntity entity : guestNetworkEntityList) {
            NetworkEntity network = networkMapper.selectById(entity.getNetworkId());
            if (network.getStatus() != cn.chenjun.cloud.management.util.Constant.NetworkStatus.READY) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getName() + "]网络[" + network.getName() + "]未就绪:" + network.getStatus());
            }
            if (network.getBasicNetworkId() > 0) {
                NetworkEntity parentNetwork = networkMapper.selectById(entity.getNetworkId());
                if (parentNetwork.getStatus() != cn.chenjun.cloud.management.util.Constant.NetworkStatus.READY) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getName() + "]网络[" + parentNetwork.getName() + "]未就绪:" + network.getStatus());
                }
            }
            OsNic nic = OsNic.builder()
                    .mac(entity.getMac())
                    .driveType(entity.getDriveType())
                    .name(guest.getName())
                    .deviceId(baseDeviceId + entity.getDeviceId())
                    .bridgeName(network.getBridge())
                    .vlanId(network.getVlanId())
                    .build();
            networkInterfaces.add(nic);
        }
        return networkInterfaces;
    }
}