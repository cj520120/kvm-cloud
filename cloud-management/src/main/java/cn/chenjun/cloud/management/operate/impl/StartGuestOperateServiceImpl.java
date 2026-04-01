package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestInfo;
import cn.chenjun.cloud.common.bean.GuestStartRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.common.util.JinjavaParser;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.operate.bean.StartGuestOperate;
import cn.chenjun.cloud.management.operate.impl.cloud.CloudInitService;
import cn.chenjun.cloud.management.operate.impl.cloud.bean.CloudData;
import cn.chenjun.cloud.management.servcie.bean.GuestExtern;
import cn.chenjun.cloud.management.util.*;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 启动虚拟机
 *
 * @author chenjun
 */
@Component
@Slf4j
public class StartGuestOperateServiceImpl extends AbstractOsOperateService<StartGuestOperate, ResultUtil<GuestInfo>> {

    @Autowired
    private List<CloudInitService> cloudInitServices;

    @Override
    public boolean requireLock() {
        return true;
    }

    @Override
    public void operate(StartGuestOperate param) {
        GuestEntity guest = guestDao.findById(param.getGuestId());
        if (guest.getStatus() != Constant.GuestStatus.STARTING) {
            throw new CodeException(ErrorCode.GUEST_NOT_STOP, "虚拟机[" + guest.getName() + "]状态不正确:" + guest.getStatus());
        }
        int hostRole = guest.getType() == Constant.GuestType.COMPONENT ? HostRole.MASTER : HostRole.WORK;
        log.info("开始启动主机,guestId={} hostId={} hostRole={}", guest.getGuestId(), param.getHostId(), hostRole);

        HostEntity host = this.allocateService.allocateHost(hostRole, guest.getLastHostId(), guest.getArch(), param.getHostId(), guest.getCpu(), guest.getMemory());
        Map<String, Object> systemConfig = this.loadGuestConfig(param.getHostId(), param.getGuestId());
        List<GuestNetworkEntity> guestNetworkEntityList = guestNetworkDao.listByAllocate(Constant.NetworkAllocateType.GUEST, guest.getGuestId());
        List<String> deviceXmlList = new ArrayList<>();
        List<String> metaDataXmlList = new ArrayList<>();
        deviceXmlList.add(this.buildCdXml(guest, systemConfig));

        GuestStartRequest request = GuestStartRequest.builder().name(guest.getName()).build();
        Optional<CloudInitService> cloudInitServiceOptional = cloudInitServices.stream().filter(cloudInitService -> Objects.equals(cloudInitService.getSupportType(), guest.getType())).findFirst();
        if (cloudInitServiceOptional.isPresent()) {
            CloudData cloudInitData = cloudInitServiceOptional.get().build(guest, host);
            if (cloudInitData != null) {
                if (!ObjectUtils.isEmpty(cloudInitData.getData())) {
                    String cloudInitPath = this.configService.getConfig(ConfigKey.SYSTEM_COMPONENT_CLOUD_INIT_PATH);
                    if (cloudInitPath.endsWith("/")) {
                        cloudInitPath = cloudInitPath.substring(0, cloudInitPath.length() - 1);
                    }
                    cloudInitPath = String.format("%s/%s.iso", cloudInitPath, guest.getName());
                    deviceXmlList.add(this.buildCloudInitXml(guest, cloudInitPath, systemConfig));
                    metaDataXmlList.add(this.buildFileMeta(cloudInitPath, cloudInitData.getData(), true));
                }
                request.setWaitCloudInit(cloudInitData.isWaiting());

            }
            int expireMinutes = this.configService.getConfig(ConfigKey.SYSTEM_COMPONENT_QMA_EXECUTE_TIMEOUT_MINUTES);
            request.setWaitCloudInitTimeoutSeconds((int) TimeUnit.MINUTES.toSeconds(expireMinutes));
        }
        deviceXmlList.addAll(this.buildDiskListXml(guest, systemConfig));
        deviceXmlList.addAll(this.buildInterfaceListXml(guest, guestNetworkEntityList, systemConfig));
        GuestExtern extern = GsonBuilderUtil.create().fromJson(guest.getExtern(), GuestExtern.class);
        if (extern.getGraphics() == null) {
            extern.setGraphics(GuestExternUtil.buildVncParam(guest, "", ""));
        }
        guest.setHostId(host.getHostId());
        guest.setLastHostId(host.getHostId());
        guest.setLastStartTime(new Date());
        this.guestDao.update(guest);
        this.allocateService.initHostAllocate();
        SchemeEntity scheme = this.schemeDao.findById(guest.getSchemeId());
        String tpl = (String) systemConfig.get(ConfigKey.VM_DOMAIN_TPL + "." + host.getArch());

        if (ObjectUtil.isEmpty(tpl)) {
            tpl = (String) systemConfig.get(ConfigKey.VM_DOMAIN_TPL);
        }

        String xml = DomainUtil.buildDomainXml(tpl, systemConfig, guest, host, scheme, extern.getGraphics().getPassword(), deviceXmlList, metaDataXmlList);
        request.setXml(xml);
        this.asyncInvoker(host, param, Constant.Command.GUEST_START, request);

    }

    private String buildFileMeta(String path, String content, boolean cleanFlag) {
        String tpl = ResourceUtil.readUtf8Str("tpl/kvm/vm/meta/file-meta.xml.json");
        String flag = cleanFlag ? Constant.Enable.YES : Constant.Enable.NO;
        Map<String, Object> map = new HashMap<>();
        map.put("path", path);
        map.put("content", content);
        map.put("cleanup_flag", flag);
        return JinjavaParser.create().render(tpl, map);
    }

    protected String buildCloudInitXml(GuestEntity guest, String path, Map<String, Object> systemConfig) {
        String tpl = systemConfig.get(ConfigKey.SYSTEM_COMPONENT_CLOUD_INIT_TPL).toString();
        Map<String, Object> map = new HashMap<>();
        map.put("path", path);
        return JinjavaParser.create().render(tpl, map);
    }


    @Override
    public void onFinish(StartGuestOperate param, ResultUtil<GuestInfo> resultUtil) {
        GuestEntity guest = guestDao.findById(param.getGuestId());
        if (guest != null && guest.getStatus() == Constant.GuestStatus.STARTING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                guest.setStatus(Constant.GuestStatus.RUNNING);
                //写入系统vnc
                GuestInfo guestInfo = resultUtil.getData();
                GuestExtern extern = GsonBuilderUtil.create().fromJson(guest.getExtern(), GuestExtern.class);
                if (extern.getGraphics() == null) {
                    extern.setGraphics(GuestExternUtil.buildVncParam(guest, "", ""));
                }

                HostEntity host = this.hostDao.findById(guest.getHostId());
                extern.getGraphics().setHost(host.getHostIp());
                extern.getGraphics().setPort(String.valueOf(guestInfo.getGraphics().getPort()));
                extern.getGraphics().setProtocol(guestInfo.getGraphics().getProtocol());
                guest.setExtern(GsonBuilderUtil.create().toJson(extern));
                this.guestDao.update(guest);
            } else {
                guest.setHostId(0);
                guest.setStatus(Constant.GuestStatus.STOP);
            }
            this.guestDao.update(guest);
            this.allocateService.initHostAllocate();
        }
        NotifyContextHolderUtil.append(NotifyData.<ResultUtil<GuestEntity>>builder().id(param.getGuestId()).type(Constant.NotifyType.GUEST_START_CALLBACK_NOTIFY).data(ResultUtil.<GuestEntity>builder().code(resultUtil.getCode()).message(resultUtil.getMessage()).data(guest).build()).build());
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());

    }

    protected List<String> buildDiskListXml(GuestEntity guest, Map<String, Object> sysconfig) {
        List<VolumeEntity> volumes = volumeDao.listByGuestId(guest.getGuestId());

        List<String> disks = new ArrayList<>();
        Map<Integer, StorageEntity> storageMap = Maps.newHashMap();
        volumes.sort(Comparator.comparingInt(VolumeEntity::getDeviceId));
        for (VolumeEntity volume : volumes) {
            if (volume.getStatus() != Constant.VolumeStatus.READY) {
                throw new CodeException(ErrorCode.VOLUME_NOT_READY, "虚拟机[" + guest.getDescription() + "]磁盘[" + volume.getName() + "]未就绪:" + volume.getStatus());
            }
            if (Objects.equals(volume.getDevice(), Constant.DeviceType.DISK)) {
                StorageEntity storage = storageMap.computeIfAbsent(volume.getStorageId(), storageId -> {
                    StorageEntity storageEntity = this.storageDao.findById(storageId);
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
            } else if (Objects.equals(volume.getDevice(), Constant.DeviceType.BLOCK)) {
                Map<String, Object> volumeConfigMap = this.loadGuestConfig(guest.getBindHostId(), guest.getGuestId());
                disks.add(this.buildBlockDiskXml(guest, volume, volume.getDeviceId(), volume.getDeviceDriver(), volumeConfigMap));
            } else if (Objects.equals(volume.getDevice(), Constant.DeviceType.FILE)) {
                Map<String, Object> volumeConfigMap = this.loadGuestConfig(guest.getBindHostId(), guest.getGuestId());
                disks.add(this.buildHostFileXml(guest, volume, volume.getDeviceId(), volume.getDeviceDriver(), volumeConfigMap));
            } else {
                throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的磁盘类型[" + volume.getDevice() + "]");
            }
        }
        return disks;
    }

    protected List<String> buildInterfaceListXml(GuestEntity guest, List<GuestNetworkEntity> guestNetworkEntityList, Map<String, Object> systemConfig) {
        guestNetworkEntityList.sort(Comparator.comparingInt(GuestNetworkEntity::getDeviceId));
        List<String> networkInterfaces = new ArrayList<>();
        for (GuestNetworkEntity entity : guestNetworkEntityList) {
            NetworkEntity network = networkDao.findById(entity.getNetworkId());
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

    @Override
    public int getType() {
        return Constant.OperateType.START_GUEST;
    }

}
