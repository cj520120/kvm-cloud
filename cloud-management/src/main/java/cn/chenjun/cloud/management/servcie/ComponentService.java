package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.dao.ComponentGuestDao;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.operate.bean.CreateGuestOperate;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.servcie.bean.GuestExtern;
import cn.chenjun.cloud.management.util.*;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author chenjun
 */
@Slf4j
@Service
public class ComponentService extends AbstractService {
    @Autowired
    private ComponentGuestDao componentGuestDao;
    @Autowired
    private AllocateService allocateService;
    @Autowired
    private GuestService guestService;

    @Transactional(rollbackFor = Exception.class)
    public ComponentGuestEntity createComponentGuest(ComponentEntity component, TemplateEntity template, HostEntity host) {
        int systemCpu = configService.getConfig(ConfigKey.SYSTEM_COMPONENT_CPU);
        int systemCpuShare = configService.getConfig(ConfigKey.SYSTEM_COMPONENT_CPU_SHARE);
        long systemMemory = (int) configService.getConfig(ConfigKey.SYSTEM_COMPONENT_MEMORY) * 1024;
        String bootStrapTypeStr = this.configService.getConfig(ConfigKey.SYSTEM_COMPONENT_BOOTSTRAP_TYPE);
        int bootStrapType;
        switch (bootStrapTypeStr) {
            case Constant.BootstrapType.UEFI_STR:
                bootStrapType = Constant.BootstrapType.UEFI;
                break;
            default:
                bootStrapType = Constant.BootstrapType.BIOS;
                break;
        }
        List<StorageEntity> storageList = this.storageDao.listAll();
        storageList.removeIf(storage -> (storage.getSupportCategory() & Constant.StorageCategory.VOLUME) != Constant.StorageCategory.VOLUME);
        storageList.removeIf(storage -> (storage.getStatus() & Constant.StorageStatus.READY) != Constant.StorageStatus.READY);
        storageList.removeIf(storage -> !Objects.equals(storage.getHostId(), host.getHostId()));
        StorageEntity storage = storageList.stream().findFirst().orElseGet(() -> this.allocateService.allocateStorage(cn.chenjun.cloud.common.util.Constant.StorageCategory.VOLUME, 0));
        GuestEntity guest = GuestEntity.builder()
                .name(NameUtil.generateGuestName())
                .groupId(0)
                .templateId(template.getTemplateId())
                .uuid(UUID.randomUUID().toString())
                .description(Constant.ComponentType.getComponentName(component.getComponentType()).toUpperCase() + " Guest[" + component.getNetworkId() + "]")
                .systemCategory(Constant.SystemCategory.CENTOS)
                .bootstrapType(bootStrapType)
                .cpu(systemCpu)
                .share(systemCpuShare)
                .memory(systemMemory)
                .cdRoom(0)
                .bindHostId(host.getHostId())
                .hostId(0)
                .lastHostId(0)
                .schemeId(0)
                .arch(host.getArch())
                .otherId(component.getComponentId())
                .guestIp("")
                .networkId(component.getNetworkId())
                .extern("{}")
                .type(cn.chenjun.cloud.common.util.Constant.GuestType.COMPONENT)
                .status(cn.chenjun.cloud.common.util.Constant.GuestStatus.CREATING)
                .build();
        this.guestDao.insert(guest);
        GuestExtern extern = new GuestExtern();
        extern.setMetaData(GuestExternUtil.buildMetaDataParam(guest, Constant.ComponentType.getComponentName(component.getComponentType()).toLowerCase()));
        extern.setVnc(GuestExternUtil.buildVncParam(guest, "", "5900"));
        guest.setExtern(GsonBuilderUtil.create().toJson(extern));
        this.guestDao.update(guest);
        String volumeType = this.configService.getConfig(ConfigKey.DEFAULT_DISK_TYPE);
        if (Objects.equals(storage.getType(), cn.chenjun.cloud.common.util.Constant.StorageType.CEPH_RBD)) {
            volumeType = cn.chenjun.cloud.common.util.Constant.VolumeType.RAW;
        }
        String nicDriveType = configService.getConfig(ConfigKey.SYSTEM_COMPONENT_NETWORK_DRIVER);
        String diskDriveType = configService.getConfig(ConfigKey.SYSTEM_COMPONENT_DISK_DRIVER);
        String volumeName = NameUtil.generateVolumeName();
        VolumeEntity volume = VolumeEntity.builder()
                .description("ROOT-" + guest.getGuestId())
                .capacity(0L)
                .hostId(storage.getHostId())
                .storageId(storage.getStorageId())
                .name(volumeName)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .templateId(template.getTemplateId())
                .allocation(0L)
                .capacity(0L)
                .guestId(guest.getGuestId())
                .deviceId(0)
                .deviceDriver(diskDriveType)
                .status(Constant.VolumeStatus.CREATING)
                .device(Constant.DeviceType.DISK)
                .serial(DiskSerialUtil.generateDiskSerial())
                .build();
        this.volumeDao.insert(volume);
        NetworkEntity network = this.networkDao.findById(component.getNetworkId());

        if (Objects.equals(network.getType(), cn.chenjun.cloud.common.util.Constant.NetworkType.VLAN)) {
            this.allocateService.allocateNetwork(network.getBasicNetworkId(), guest.getGuestId(), Constant.NetworkAllocateType.GUEST, 0, nicDriveType, "Component Guest Basic Nic");
            GuestNetworkEntity currentGuestNetwork = this.allocateService.allocateNetwork(network.getNetworkId(), guest.getGuestId(), Constant.NetworkAllocateType.GUEST, 1, nicDriveType, "Component Guest Basic Nic");
            guest.setGuestIp(currentGuestNetwork.getIp());
        } else {
            GuestNetworkEntity currentGuestNetwork = this.allocateService.allocateNetwork(network.getNetworkId(), guest.getGuestId(), Constant.NetworkAllocateType.GUEST, 0, nicDriveType, "Component Guest Basic Nic");
            guest.setGuestIp(currentGuestNetwork.getIp());
        }

        this.guestDao.update(guest);
        BaseOperateParam operateParam = CreateGuestOperate.builder()
                .guestId(guest.getGuestId())
                .templateId(template.getTemplateId())
                .volumeId(volume.getVolumeId())
                .start(true)
                .hostId(0)
                .id(UUID.randomUUID().toString())
                .title("创建系统主机[" + guest.getDescription() + "]")
                .build();
        this.operateTask.addTask(operateParam);
        ComponentGuestEntity componentGuest = ComponentGuestEntity.builder()
                .guestId(guest.getGuestId())
                .componentId(component.getComponentId())
                .componentType(component.getComponentType())
                .componentVersion(SystemUtil.COMPONENT_VERSION)
                .lastActiveTime(new Date())
                .createTime(new Date())
                .hostId(host.getHostId())
                .sessionId("")
                .status(Constant.ComponentGuestStatus.CREATING)
                .build();
        this.componentGuestDao.insert(componentGuest);

        this.notifyService.publish(NotifyData.<Void>builder().id(componentGuest.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
        return componentGuest;
    }

    public ComponentEntity getComponentById(Integer componentId) {
        ComponentEntity component = this.componentDao.findById(componentId);
        if (component == null) {
            throw new CodeException(ErrorCode.NETWORK_COMPONENT_NOT_FOUND, "组件不存在");
        }
        return component;
    }

    public List<ComponentEntity> listComponentByIds(List<Integer> componentIds) {
        return this.componentDao.listByIds(componentIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean checkNetworkIsEnableComponent(int networkId) {

        List<ConfigQuery> queryList = new ArrayList<>();
        queryList.add(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).id(0).build());
        queryList.add(ConfigQuery.builder().type(Constant.ConfigType.NETWORK).id(networkId).build());
        boolean isCheckComponentEnable = Objects.equals(this.configService.getConfig(queryList, ConfigKey.SYSTEM_COMPONENT_ENABLE), Constant.Enable.YES);
        if (!isCheckComponentEnable) {
            NetworkEntity network = this.networkDao.findById(networkId);
            if (network.getStatus() != cn.chenjun.cloud.common.util.Constant.NetworkStatus.INSTALL) {
                network.setStatus(Constant.NetworkStatus.READY);
                this.networkDao.update(network);
                this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());

            }
            log.info("网络不需要安装组件，network_id={}", network.getNetworkId());
            return false;
        }
        return true;
    }


    @Transactional(rollbackFor = Exception.class)
    public boolean checkNetworkComponentReady(List<HostEntity> masterHostList, ComponentEntity component, NetworkEntity network) {
        List<ComponentGuestEntity> componentGuestList = this.componentGuestDao.listByComponentId(component.getComponentId());
        Iterator<ComponentGuestEntity> iterator = componentGuestList.iterator();
        while (iterator.hasNext()) {
            ComponentGuestEntity componentGuest = iterator.next();
            GuestEntity guest = this.guestDao.findById(componentGuest.getGuestId());
            if (guest == null) {
                this.componentGuestDao.deleteById(componentGuest.getComponentGuestId());
                componentGuestList.remove(componentGuestList);
                log.warn("组件虚拟机不存在，清理组件信息，guest_id={}", componentGuest.getGuestId());
            } else if (Objects.equals(SystemUtil.COMPONENT_VERSION, componentGuest.getComponentVersion())) {
                checkComponentGuestStatus(guest, componentGuest);
            }
        }
        boolean componentReady = false;
        List<Integer> masterHostIds = masterHostList.stream().map(HostEntity::getHostId).collect(Collectors.toList());
        for (ComponentGuestEntity componentGuest : componentGuestList) {
            if (masterHostIds.contains(componentGuest.getHostId()) && Objects.equals(SystemUtil.COMPONENT_VERSION, componentGuest.getComponentVersion())) {
                //只检查主节点是否就绪
                componentReady |= Objects.equals(componentGuest.getStatus(), Constant.ComponentGuestStatus.ONLINE);
            }
        }
        if (Objects.equals(Constant.ComponentType.ROUTE, component.getComponentType())) {
            if (componentReady && !Objects.equals(Constant.NetworkStatus.READY, network.getStatus())) {
                network.setStatus(Constant.NetworkStatus.READY);
                this.networkDao.update(network);
                this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(Constant.NotifyType.UPDATE_NETWORK).build());
            } else if (!componentReady && !Objects.equals(Constant.NetworkStatus.INSTALL, network.getStatus())) {
                network.setStatus(Constant.NetworkStatus.INSTALL);
                this.networkDao.update(network);
                this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(Constant.NotifyType.UPDATE_NETWORK).build());
            }
        }
        return componentReady;
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkAndCreateMissComponentGuest(int componentId, int hostId) {
        ComponentEntity component = this.componentDao.findById(componentId);
        if (component == null) {
            return;
        }
        HostEntity host = this.hostDao.findById(hostId);
        if (host.getStatus() != Constant.HostStatus.ONLINE) {
            log.warn("主节点未就绪,等待就绪后再进行安装,host_id={} component_type={}", host.getHostId(), component.getComponentType());
            return;
        }
        if (!HostRole.isMaster(host.getRole())) {
            log.info("主机不是Master节点，跳过组件安装,host_id={} component_type={}", host.getHostId(), component.getComponentType());
            return;
        }
        List<ComponentGuestEntity> componentGuests = this.componentGuestDao.listByComponentIdAndHostId(componentId, hostId);
        for (ComponentGuestEntity componentGuest : componentGuests) {
            if (Objects.equals(SystemUtil.COMPONENT_VERSION, componentGuest.getComponentVersion())) {
                return;
            }
        }
        TemplateEntity template = this.getSystemTemplate(host.getArch());
        if (template == null) {
            log.warn("系统模版未就绪，等待就绪后创建系统组件,component_type={}", component.getComponentType());
            return;
        }
        log.info("Master主机未安装组件，开始安装组件虚拟机,host_id={},component_id={}", hostId, component.getComponentId());
        this.createComponentGuest(component, template, host);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cleanOldComponentGuest(int componentID, List<Integer> masterHostIds) {
        List<ComponentGuestEntity> componentGuestList = this.componentGuestDao.listByComponentId(componentID);
        for (ComponentGuestEntity componentGuest : componentGuestList) {
            if (!masterHostIds.contains(componentGuest.getHostId()) || !Objects.equals(SystemUtil.COMPONENT_VERSION, componentGuest.getComponentVersion())) {
                log.info("清理组件虚拟机,guest_id={}", componentGuest.getGuestId());
                GuestEntity guest = this.guestDao.findById(componentGuest.getGuestId());
                if (guest == null) {
                    this.componentGuestDao.deleteById(componentGuest.getComponentGuestId());
                    continue;
                }
                switch (guest.getStatus()) {
                    case Constant.GuestStatus.ERROR:
                    case Constant.GuestStatus.STOP:
                        this.guestService.destroyGuest(componentGuest.getGuestId());
                        componentGuest.setStatus(Constant.ComponentGuestStatus.DESTROY);
                        this.componentGuestDao.update(componentGuest);
                        break;
                    case Constant.GuestStatus.STOPPING:
                        break;
                    default:
                        this.guestService.shutdown(componentGuest.getGuestId(), true);
                        break;

                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void cleanUnlinkComponentGuest(int componentId) {
        List<GuestEntity> systemGuestList = this.guestDao.listComponentGuest(componentId);
        List<ComponentGuestEntity> componentGuestList = this.componentGuestDao.listByComponentId(componentId);
        List<Integer> componentGuestIds = componentGuestList.stream().map(ComponentGuestEntity::getGuestId).collect(Collectors.toList());
        for (GuestEntity systemGuest : systemGuestList) {
            //删除不存在的组件虚拟机
            if (!componentGuestIds.contains(systemGuest.getGuestId())) {
                log.warn("组件配置不存在，清理组件虚拟机信息,guest_id={}", systemGuest.getGuestId());
                switch (systemGuest.getStatus()) {
                    case Constant.GuestStatus.ERROR:
                    case Constant.GuestStatus.STOP:
                        this.guestService.destroyGuest(systemGuest.getGuestId());
                        break;
                    case Constant.GuestStatus.STARTING:
                    case Constant.GuestStatus.RUNNING:
                    case Constant.GuestStatus.REBOOT:
                        this.guestService.shutdown(systemGuest.getGuestId(), true);
                        break;
                    case Constant.GuestStatus.STOPPING:
                        break;
                }
            }
        }
    }

    private void checkComponentGuestStatus(GuestEntity guest, ComponentGuestEntity componentGuest) {
        switch (guest.getStatus()) {
            case Constant.GuestStatus.CREATING:
                componentGuest.setLastActiveTime(new Date());
                componentGuest.setStatus(Constant.ComponentGuestStatus.CREATING);
                break;
            case Constant.GuestStatus.STARTING:
            case Constant.GuestStatus.REBOOT:
                componentGuest.setLastActiveTime(new Date());
                if (componentGuest.getStatus() != Constant.ComponentGuestStatus.ONLINE) {
                    componentGuest.setStatus(Constant.ComponentGuestStatus.INSTALL);
                } else {
                    log.info("组件虚拟机已经在线，无需更改状态,guest_id={}", guest.getGuestId());
                }
                break;
            case Constant.GuestStatus.STOPPING:
                componentGuest.setLastActiveTime(new Date());
                componentGuest.setStatus(Constant.ComponentGuestStatus.OFFLINE);
                break;
            case Constant.GuestStatus.STOP:
                componentGuest.setLastActiveTime(new Date());
                componentGuest.setErrorCount(componentGuest.getErrorCount() + 1);
                int maxErrorCount = configService.getConfig(ConfigKey.SYSTEM_COMPONENT_CONNET_MAX_ERROR_COUNT);
                if (componentGuest.getErrorCount() >= maxErrorCount) {
                    componentGuest.setStatus(Constant.ComponentGuestStatus.DESTROY);
                    this.guestService.destroyGuest(guest.getGuestId());
                    log.warn("组件虚拟机失败次数过多，销毁虚拟机,guest_id={}", guest.getGuestId());
                } else {
                    componentGuest.setStatus(Constant.ComponentGuestStatus.INSTALL);
                    this.guestService.start(guest.getGuestId(), componentGuest.getHostId());
                }
                break;
            case Constant.GuestStatus.ERROR:
                componentGuest.setLastActiveTime(new Date());
                componentGuest.setStatus(Constant.ComponentGuestStatus.ERROR);
                this.guestService.destroyGuest(guest.getGuestId());
                break;
            case Constant.GuestStatus.DESTROY:
                componentGuest.setLastActiveTime(new Date());
                componentGuest.setStatus(Constant.ComponentGuestStatus.DESTROY);
                break;
            case Constant.GuestStatus.RUNNING:
                if (componentGuest.getStatus() != Constant.ComponentGuestStatus.ONLINE) {
                    int timeoutSeconds = configService.getConfig(ConfigKey.SYSTEM_COMPONENT_CONNET_TIMEOUT_SECONDS);
                    //如果组件不在线，且最后活跃时间超过了1分钟，则表示组件内部可能出现了错误，需要停止组件,并且设置错误次数+1
                    if (componentGuest.getLastActiveTime().getTime() + TimeUnit.SECONDS.toMillis(timeoutSeconds) < System.currentTimeMillis()) {
                        //检测当前任务启动时间是否超过了1分钟，超过1分钟后才重置错误次数，否则会导致异常重启的情况发生
                        if (System.currentTimeMillis() - SystemUtil.START_TIME > TimeUnit.MINUTES.toMillis(1)) {
                            componentGuest.setLastActiveTime(new Date());
                            componentGuest.setStatus(Constant.ComponentGuestStatus.OFFLINE);
                            this.guestService.shutdown(guest.getGuestId(), false);
                            log.warn("组件虚拟机长时间未响应，停止虚拟机,guest_id={}", guest.getGuestId());
                        }
                    }
                }
                break;
            default:
                break;
        }
        this.componentGuestDao.update(componentGuest);
    }

    private TemplateEntity getSystemTemplate(String arch) {
        List<TemplateEntity> templateList = this.templateDao.listAll();
        templateList = templateList.stream().filter(template -> template.getArch().equals(arch) && template.getTemplateType() == Constant.TemplateType.SYSTEM && template.getStatus() == cn.chenjun.cloud.common.util.Constant.TemplateStatus.READY).collect(Collectors.toList());
        if (templateList.isEmpty()) {
            return null;
        } else {
            Collections.shuffle(templateList);
            return templateList.get(0);
        }
    }

    public List<ComponentEntity> listComponentByNetworkId(Integer networkId) {
        return this.componentDao.listByNetworkId(networkId);
    }

    public ComponentGuestEntity findComponentGuestById(int componentGuestId) {
        return this.componentGuestDao.findById(componentGuestId);
    }

    public void updateComponentGuest(ComponentGuestEntity componentGuest) {
        this.componentGuestDao.update(componentGuest);
    }
}
