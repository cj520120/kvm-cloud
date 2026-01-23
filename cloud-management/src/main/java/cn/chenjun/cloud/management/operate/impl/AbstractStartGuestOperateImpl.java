package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestInfo;
import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.bean.GuestStartRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.util.*;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
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
public abstract class AbstractStartGuestOperateImpl<T extends BaseOperateParam> extends AbstractOsOperate<T, ResultUtil<GuestInfo>> {


    public void start(int hostId, int guestId, T param) {
        GuestEntity guest = guestMapper.selectById(guestId);
        if (guest.getStatus() != Constant.GuestStatus.STARTING) {
            throw new CodeException(ErrorCode.GUEST_NOT_STOP, "虚拟机[" + guest.getName() + "]状态不正确:" + guest.getStatus());
        }
        int hostRole=guest.getType()== Constant.GuestType.COMPONENT? HostRole.COMPONENT :HostRole.USER;
        log.info("开始启动主机,guestId={} hostId={} hostRole={}",guest.getGuestId(),hostId,hostRole);
        HostEntity host = this.allocateService.allocateHost(hostRole,guest.getLastHostId(), hostId, guest.getCpu(), guest.getMemory());
        Map<String, Object> systemConfig = this.loadGuestConfig(hostId, guestId);
        List<String> deviceXmlList = new ArrayList<>();
        deviceXmlList.add(this.buildCdXml(guest, systemConfig));
        deviceXmlList.addAll(this.buildDiskListXml(guest, systemConfig));
        deviceXmlList.addAll(this.buildInterfaceListXml(guest, systemConfig));

        Map<String, Map<String, String>> extern = GsonBuilderUtil.create().fromJson(guest.getExtern(), new TypeToken<Map<String, Map<String, String>>>() {
        }.getType());
        Map<String, String> vncMap = extern.computeIfAbsent(GuestExternNames.VNC, k -> GuestExternUtil.buildVncParam(guest, "", ""));

        guest.setHostId(host.getHostId());
        guest.setLastHostId(host.getHostId());
        guest.setLastStartTime(new Date());
        this.guestMapper.updateById(guest);
        this.allocateService.initHostAllocate();
        SchemeEntity scheme = this.schemeMapper.selectById(guest.getSchemeId());
        String tpl = (String) systemConfig.get(ConfigKey.VM_DOMAIN_TPL);
        String xml = DomainUtil.buildDomainXml(tpl, systemConfig, guest, host, scheme, vncMap.get(GuestExternNames.VncNames.PASSWORD), deviceXmlList);
        GuestStartRequest request = GuestStartRequest.builder()
                .name(guest.getName())
                .qmaRequest(this.buildQmaRequest(param, systemConfig))
                .xml(xml)
                .build();
        this.asyncInvoker(host, param, Constant.Command.GUEST_START, request);

    }

    protected void finish(int guestId, ResultUtil<GuestInfo> resultUtil) {
        GuestEntity guest = guestMapper.selectById(guestId);
        if (guest != null && guest.getStatus() == Constant.GuestStatus.STARTING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                guest.setStatus(Constant.GuestStatus.RUNNING);
                //写入系统vnc
                GuestInfo guestInfo = resultUtil.getData();
                Map<String, Map<String, String>> extern = GsonBuilderUtil.create().fromJson(guest.getExtern(), new TypeToken<Map<String, Map<String, String>>>() {
                }.getType());
                Map<String, String> vncMap = extern.computeIfAbsent(GuestExternNames.VNC, k -> GuestExternUtil.buildVncParam(guest, "", ""));
                HostEntity host = this.hostMapper.selectById(guest.getHostId());
                vncMap.put(GuestExternNames.VncNames.HOST, host.getHostIp());
                vncMap.put(GuestExternNames.VncNames.PORT, String.valueOf(guestInfo.getVnc()));
                guest.setExtern(GsonBuilderUtil.create().toJson(extern));
                this.guestMapper.updateById(guest);
            } else {
                guest.setHostId(0);
                guest.setStatus(Constant.GuestStatus.STOP);
            }
            this.guestMapper.updateById(guest);
            this.allocateService.initHostAllocate();
        }
        this.notifyService.publish(NotifyData.<ResultUtil<GuestEntity>>builder().id(guestId).type(Constant.NotifyType.GUEST_START_CALLBACK_NOTIFY).data(ResultUtil.<GuestEntity>builder().code(resultUtil.getCode()).message(resultUtil.getMessage()).data(guest).build()).build());
        this.notifyService.publish(NotifyData.<Void>builder().id(guestId).type(Constant.NotifyType.UPDATE_GUEST).build());

    }

    protected List<String> buildDiskListXml(GuestEntity guest, Map<String, Object> sysconfig) {
        List<VolumeEntity> volumes = volumeMapper.selectList(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.GUEST_ID, guest.getGuestId()));

        List<String> disks = new ArrayList<>();
        Map<Integer, StorageEntity> storageMap = Maps.newHashMap();
        volumes.sort(Comparator.comparingInt(VolumeEntity::getDeviceId));
        for (VolumeEntity volume : volumes) {
            if (volume.getStatus() != Constant.VolumeStatus.READY) {
                throw new CodeException(ErrorCode.VOLUME_NOT_READY, "虚拟机[" + guest.getDescription() + "]磁盘[" + volume.getName() + "]未就绪:" + volume.getStatus());
            }
            if(Objects.equals(volume.getDevice(),Constant.DeviceType.DISK)) {
                StorageEntity storage = storageMap.computeIfAbsent(volume.getStorageId(), storageId -> {
                    StorageEntity storageEntity = this.storageMapper.selectById(storageId);
                    if (storageEntity == null) {
                        throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "虚拟机[" + guest.getDescription() + "]磁盘[" + volume.getName() + "]所属存储池不存在");
                    }
                    if (storageEntity.getStatus() != Constant.StorageStatus.READY) {
                        throw new CodeException(ErrorCode.STORAGE_NOT_READY, "虚拟机[" + guest.getDescription() + "]磁盘[" + volume.getName() + "]所属存储池未就绪:" + storageEntity.getName());
                    }
                    return storageEntity;
                });
                Map<String, Object> volumeConfigMap = this.loadVolumeConfig(storage.getStorageId(), volume.getVolumeId());
                Map<String, Object> configMap = new HashMap<>();
                configMap.putAll(sysconfig);
                configMap.putAll(volumeConfigMap);
                disks.add(this.buildDiskXml(guest, storage, volume, volume.getDeviceId(), volume.getDeviceDriver(), configMap));
            }else{
                Map<String, Object> volumeConfigMap = this.loadGuestConfig(guest.getBindHostId(),guest.getGuestId());
                disks.add(this.buildBlockDiskXml(guest, volume, volume.getDeviceId(), volume.getDeviceDriver(), volumeConfigMap));
            }
        }
        return disks;
    }


    protected abstract GuestQmaRequest buildQmaRequest(T param, Map<String, Object> systemConfig);

    protected List<String> buildInterfaceListXml(GuestEntity guest, Map<String, Object> systemConfig) {
        List<GuestNetworkEntity> guestNetworkEntityList = guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.ALLOCATE_ID, guest.getGuestId()).eq(GuestNetworkEntity.ALLOCATE_TYPE, Constant.NetworkAllocateType.GUEST));
        guestNetworkEntityList.sort(Comparator.comparingInt(GuestNetworkEntity::getDeviceId));
        List<String> networkInterfaces = new ArrayList<>();
        for (GuestNetworkEntity entity : guestNetworkEntityList) {
            NetworkEntity network = networkMapper.selectById(entity.getNetworkId());
            if (!guest.getType().equals(Constant.GuestType.COMPONENT)) {
                if (network.getStatus() != Constant.NetworkStatus.READY) {
                    throw new CodeException(ErrorCode.NETWORK_NOT_READY, "虚拟机[" + guest.getDescription() + "]网络[" + network.getName() + "]未就绪.");
                }
            }
            networkInterfaces.add(this.buildInterfaceXml(network, entity, systemConfig));
        }
        return networkInterfaces;
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<GuestInfo>>() {
        }.getType();
    }
}
