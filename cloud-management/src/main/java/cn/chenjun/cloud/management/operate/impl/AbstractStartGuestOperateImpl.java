package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestInfo;
import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.bean.GuestStartRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.servcie.VncService;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.DomainUtil;
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
public abstract class AbstractStartGuestOperateImpl<T extends BaseOperateParam> extends AbstractOsOperate<T, ResultUtil<GuestInfo>> {


    @Autowired
    private VncService vncService;

    @Autowired
    private ConfigService configService;


    public void start(int hostId, int guestId, T param) {
        GuestEntity guest = guestMapper.selectById(guestId);
        if (guest.getStatus() != cn.chenjun.cloud.management.util.Constant.GuestStatus.STARTING) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getName() + "]状态不正确:" + guest.getStatus());
        }
        HostEntity host = this.allocateService.allocateHost(guest.getLastHostId(), hostId, guest.getCpu(), guest.getMemory());
        Map<String, Object> systemConfig = this.loadSystemConfig(hostId, guestId);
        List<String> deviceXmlList = new ArrayList<>();
        deviceXmlList.add(this.buildCdXml(guest, systemConfig));
        deviceXmlList.addAll(this.buildDiskListXml(guest, systemConfig));
        deviceXmlList.addAll(this.buildInterfaceListXml(guest, systemConfig));
        GuestVncEntity vnc = this.vncService.getGuestVnc(guestId);
        guest.setHostId(host.getHostId());
        guest.setLastHostId(host.getHostId());
        guest.setLastStartTime(new Date());
        this.guestMapper.updateById(guest);
        this.allocateService.initHostAllocate();
        SchemeEntity scheme = this.schemeMapper.selectById(guest.getSchemeId());
        String tpl = (String) systemConfig.get(ConfigKey.VM_DOMAIN_TPL);
        String xml = DomainUtil.buildDomainXml(tpl, systemConfig, guest, host, scheme, vnc, deviceXmlList);
        GuestStartRequest request = GuestStartRequest.builder()
                .name(guest.getName())
                .qmaRequest(this.buildQmaRequest(param, systemConfig))
                .xml(xml)
                .build();
        this.asyncInvoker(host, param, Constant.Command.GUEST_START, request);

    }

    protected void finish(int guestId, ResultUtil<GuestInfo> resultUtil) {
        GuestEntity guest = guestMapper.selectById(guestId);
        if (guest != null && guest.getStatus() == cn.chenjun.cloud.management.util.Constant.GuestStatus.STARTING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                guest.setStatus(cn.chenjun.cloud.management.util.Constant.GuestStatus.RUNNING);
                //写入系统vnc
                GuestInfo guestInfo = resultUtil.getData();
                this.vncService.updateVncPort(guestId, guestInfo.getVnc());
            } else {
                guest.setHostId(0);
                guest.setStatus(cn.chenjun.cloud.management.util.Constant.GuestStatus.STOP);
            }
            this.guestMapper.updateById(guest);
            this.allocateService.initHostAllocate();
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(guestId).type(Constant.NotifyType.UPDATE_GUEST).build());

    }

    protected List<String> buildDiskListXml(GuestEntity guest, Map<String, Object> sysconfig) {
        List<GuestDiskEntity> guestDiskEntityList = guestDiskMapper.selectList(new QueryWrapper<GuestDiskEntity>().eq(GuestDiskEntity.GUEST_ID, guest.getGuestId()));
        List<String> disks = new ArrayList<>();
        Map<Integer, StorageEntity> storageMap = Maps.newHashMap();
        for (GuestDiskEntity guestDisk : guestDiskEntityList) {
            VolumeEntity volume = volumeMapper.selectById(guestDisk.getVolumeId());
            if (volume.getStatus() != cn.chenjun.cloud.management.util.Constant.VolumeStatus.READY) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]磁盘[" + volume.getName() + "]未就绪:" + volume.getStatus());
            }
            StorageEntity storage = storageMap.computeIfAbsent(volume.getStorageId(), storageId -> {
                StorageEntity storageEntity = this.storageMapper.selectById(storageId);
                if (storageEntity == null) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]磁盘[" + volume.getName() + "]所属存储池不存在");
                }
                if (storageEntity.getStatus() != cn.chenjun.cloud.management.util.Constant.StorageStatus.READY) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]磁盘[" + volume.getName() + "]所属存储池未就绪:" + storageEntity.getStatus());
                }
                return storageEntity;
            });
            disks.add(this.buildDiskXml(guest, storage, volume, guestDisk.getDeviceId(), sysconfig));
        }
        return disks;
    }


    protected abstract GuestQmaRequest buildQmaRequest(T param, Map<String, Object> systemConfig);

    protected List<String> buildInterfaceListXml(GuestEntity guest, Map<String, Object> systemConfig) {
        List<GuestNetworkEntity> guestNetworkEntityList = guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.ALLOCATE_ID, guest.getGuestId()).eq(GuestNetworkEntity.ALLOCATE_TYPE, cn.chenjun.cloud.management.util.Constant.NetworkAllocateType.GUEST));
        guestNetworkEntityList.sort(Comparator.comparingInt(GuestNetworkEntity::getDeviceId));
        List<String> networkInterfaces = new ArrayList<>();
        String tpl = (String) systemConfig.get(ConfigKey.VM_INTERFACE_TPL);
        for (GuestNetworkEntity entity : guestNetworkEntityList) {
            NetworkEntity network = networkMapper.selectById(entity.getNetworkId());
            if (!guest.getType().equals(cn.chenjun.cloud.management.util.Constant.GuestType.COMPONENT)) {
                if (network.getStatus() != cn.chenjun.cloud.management.util.Constant.NetworkStatus.READY) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getName() + "]网络[" + network.getName() + "]未就绪:" + network.getStatus());
                }
            }
            if (network.getBasicNetworkId() > 0) {
                NetworkEntity parentNetwork = networkMapper.selectById(entity.getNetworkId());
                if (!guest.getType().equals(cn.chenjun.cloud.management.util.Constant.GuestType.COMPONENT)) {
                    if (parentNetwork.getStatus() != cn.chenjun.cloud.management.util.Constant.NetworkStatus.READY) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getName() + "]网络[" + parentNetwork.getName() + "]未就绪:" + network.getStatus());
                    }
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
