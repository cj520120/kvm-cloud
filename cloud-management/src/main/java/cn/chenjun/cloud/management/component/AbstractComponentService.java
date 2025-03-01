package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.BootstrapType;
import cn.chenjun.cloud.common.util.SystemCategory;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.CreateGuestOperate;
import cn.chenjun.cloud.management.operate.bean.StartComponentGuestOperate;
import cn.chenjun.cloud.management.servcie.AbstractService;
import cn.chenjun.cloud.management.servcie.AllocateService;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.servcie.GuestService;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.GuestNameUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
public abstract class AbstractComponentService<T extends ComponentQmaInitialize> extends AbstractService implements ComponentProcess {

    @Autowired
    protected AllocateService allocateService;
    @Autowired
    protected ComponentMapper componentMapper;
    @Autowired
    protected GuestService guestService;

    @Autowired
    protected ConfigService configService;

    private final List<T> componentQmaInitializeList;

    protected AbstractComponentService(List<T> componentQmaInitializeList) {
        componentQmaInitializeList.sort(Comparator.comparingInt(Ordered::getOrder));
        this.componentQmaInitializeList = componentQmaInitializeList;
    }

    /**
     * 创建系统组件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndStart(NetworkEntity network, ComponentEntity component) {

        List<HostEntity> hostList = allocateService.listAllocateHost(configService.getConfig(Constant.ConfigKey.SYSTEM_COMPONENT_CPU), (int) configService.getConfig(Constant.ConfigKey.SYSTEM_COMPONENT_MEMORY) * 1024);
        List<Integer> hostIds = hostList.stream().map(HostEntity::getHostId).collect(Collectors.toList());
        if (hostIds.isEmpty()) {
            log.info("没有可用的主机，无法启动组件:{}", this.getComponentName());
            return;
        }
        GuestEntity masterGuest = checkAndStartMasterComponent(network, component, hostIds);
        if (masterGuest == null) {
            return ;
        }

        if (masterGuest.getStatus() == Constant.GuestStatus.RUNNING) {
            if (!checkComponentSlaveNumber(component)) {
                return ;
            }
            checkAndStartSlaveComponent(component, network, hostIds);
        }
    }

    /**
     * 检测slave组件数量是否匹配
     *
     * @param component
     * @return
     */
    private boolean checkComponentSlaveNumber(ComponentEntity component) {
        List<Integer> slaveList = GsonBuilderUtil.create().fromJson(component.getSlaveGuestIds(), new TypeToken<List<Integer>>() {
        }.getType());
        boolean isUpdateSlave = false;
        while (slaveList.size() < component.getComponentSlaveNumber()) {
            //创建slave组件
            slaveList.add(0);
            isUpdateSlave = true;
        }

        if (isUpdateSlave) {
            component.setSlaveGuestIds(GsonBuilderUtil.create().toJson(slaveList));
            this.componentMapper.updateById(component);
        }

        if (slaveList.size() > component.getComponentSlaveNumber()) {
            //删除多余的slave组件
            int slaveGuestId = slaveList.remove(slaveList.size() - 1);
            if (slaveGuestId > 0) {
                GuestEntity slaveGuest = this.guestMapper.selectById(slaveGuestId);
                if (slaveGuest != null) {
                    this.destroySlaveGuest(slaveGuest);
                    //slave销毁成功后再进行后续操作
                } else {
                    component.setSlaveGuestIds(GsonBuilderUtil.create().toJson(slaveList));
                    this.componentMapper.updateById(component);
                }
            } else {
                component.setSlaveGuestIds(GsonBuilderUtil.create().toJson(slaveList));
                this.componentMapper.updateById(component);
            }
            return false;
        }
        return true;
    }

    private void checkAndStartSlaveComponent(ComponentEntity component, NetworkEntity network, List<Integer> hostIds) {
        List<Integer> slaveList = GsonBuilderUtil.create().fromJson(component.getSlaveGuestIds(), new TypeToken<List<Integer>>() {
        }.getType());
        boolean isUpdateSlave = false;
        int templateId = 0;
        for (int i = 0; i < slaveList.size() && !hostIds.isEmpty() && templateId >= 0; i++) {
            int slaveGuestId = slaveList.get(i);
            GuestEntity slaveGuest;
            if (slaveGuestId > 0) {
                //删除无效的组件
                slaveGuest = this.guestMapper.selectById(slaveGuestId);
                if (slaveGuest == null) {
                    slaveList.set(i, 0);
                    isUpdateSlave = true;
                    this.notifyService.publish(NotifyData.<Void>builder().id(slaveGuestId).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                }
            } else {
                //创建已经删除或无效的slave组件
                if (templateId == 0) {
                    templateId = getTemplateId();
                }
                slaveGuest = this.createSystemComponentGuest(component.getComponentId(), "Slave " + this.getComponentName(), network, templateId);
                slaveList.set(i, slaveGuest.getGuestId());
                isUpdateSlave = true;
                this.notifyService.publish(NotifyData.<Void>builder().id(slaveGuest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());

            }
            if (slaveGuest != null) {
                this.startComponentGuest(slaveGuest, hostIds);
            }
        }
        if (isUpdateSlave) {
            component.setSlaveGuestIds(GsonBuilderUtil.create().toJson(slaveList));
            this.componentMapper.updateById(component);
        }
    }

    private GuestEntity checkAndStartMasterComponent(NetworkEntity network, ComponentEntity component, List<Integer> hostIds) {
        GuestEntity masterGuest = this.guestMapper.selectById(component.getMasterGuestId());
        if (masterGuest == null) {
            int templateId = getTemplateId();
            if (templateId < 0) {
                return null;
            }
            masterGuest = createSystemComponentGuest(component.getComponentId(), "Master " + this.getComponentName(), network, templateId);
            this.notifyService.publish(NotifyData.<Void>builder().id(masterGuest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
            component.setMasterGuestId(masterGuest.getGuestId());
            this.componentMapper.updateById(component);
        }
        this.startComponentGuest(masterGuest, hostIds);
        return masterGuest;
    }


    private int getTemplateId() {
        int templateId;
        List<TemplateEntity> templateList = this.templateMapper.selectList(new QueryWrapper<TemplateEntity>().eq(TemplateEntity.TEMPLATE_TYPE, Constant.TemplateType.SYSTEM).eq(TemplateEntity.TEMPLATE_STATUS, Constant.TemplateStatus.READY));
        if (templateList.isEmpty()) {
            log.info("系统模版未就绪，等待就绪后创建系统组件.");
            templateId = -1;
        } else {
            Collections.shuffle(templateList);
            templateId = templateList.get(0).getTemplateId();
        }
        return templateId;
    }

    private void startComponentGuest(GuestEntity guest, List<Integer> hostIds) {
        Integer lastHostId = guest.getLastHostId();
        //优先使用上次启动的主机
        if (!hostIds.contains(lastHostId)) {
            lastHostId = hostIds.get(0);
        }
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
                guest.setStatus(Constant.GuestStatus.STARTING);
                guest.setLastHostId(lastHostId);
                guestMapper.updateById(guest);
                BaseOperateParam operateParam = StartComponentGuestOperate.builder().id(UUID.randomUUID().toString()).title("启动系统主机[" + this.getComponentName() + "]").guestId(guest.getGuestId()).hostId(lastHostId).componentType(this.getComponentType()).build();
                this.operateTask.addTask(operateParam);
                this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                break;
            case Constant.GuestStatus.ERROR:
                this.guestService.destroyGuest(guest.getGuestId());
                break;
            default:
                break;
        }
        //从可用主机中删除组件主机
        hostIds.remove((Object)lastHostId);
    }

    private void destroySlaveGuest(GuestEntity guest) {
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
            case Constant.GuestStatus.ERROR:
                this.guestService.destroyGuest(guest.getGuestId());
                break;
            case Constant.GuestStatus.RUNNING:
                this.guestService.shutdown(guest.getGuestId(), true);
                break;
            default:
                break;
        }
    }


    private GuestEntity createSystemComponentGuest(int componentId, String name, NetworkEntity network, int diskTemplateId) {
        String uid = UUID.randomUUID().toString().replace("-", "");
        int systemCpu=configService.getConfig(Constant.ConfigKey.SYSTEM_COMPONENT_CPU);
        int systemCpuShare=configService.getConfig(Constant.ConfigKey.SYSTEM_COMPONENT_CPU_SHARE);
        long systemMemory=(int)configService.getConfig(Constant.ConfigKey.SYSTEM_COMPONENT_MEMORY)*1024;
        GuestEntity guest = GuestEntity.builder()
                .name(GuestNameUtil.getName())
                .groupId(0)
                .description(name)
                .systemCategory(SystemCategory.CENTOS)
                .bootstrapType(BootstrapType.BIOS)
                .busType(cn.chenjun.cloud.common.util.Constant.DiskBus.VIRTIO)
                .cpu(systemCpu)
                .share(systemCpuShare)
                .memory(systemMemory)
                .cdRoom(0)
                .hostId(0)
                .lastHostId(0)
                .schemeId(0)
                .otherId(componentId)
                .guestIp("")
                .networkId(network.getNetworkId())
                .type(Constant.GuestType.COMPONENT)
                .status(Constant.GuestStatus.CREATING)
                .build();
        this.guestMapper.insert(guest);
        StorageEntity storage = this.allocateService.allocateStorage(Constant.StorageSupportCategory.VOLUME,0);
        String volumeType = this.configService.getConfig(Constant.ConfigKey.DEFAULT_CLUSTER_DISK_TYPE);
        if (Objects.equals(storage.getType(), cn.chenjun.cloud.common.util.Constant.StorageType.CEPH_RBD)) {
            volumeType = cn.chenjun.cloud.common.util.Constant.VolumeType.RAW;
        }
        VolumeEntity volume = VolumeEntity.builder()
                .description("ROOT-" + guest.getGuestId())
                .capacity(0L)
                .storageId(storage.getStorageId())
                .name(uid)
                .path(storage.getMountPath() + "/" + uid)
                .type(volumeType)
                .templateId(diskTemplateId)
                .allocation(0L)
                .capacity(0L)
                .status(Constant.VolumeStatus.CREATING)
                .build();
        this.volumeMapper.insert(volume);
        GuestDiskEntity guestDisk = GuestDiskEntity.builder()
                .volumeId(volume.getVolumeId())
                .guestId(guest.getGuestId())
                .deviceId(0)
                .build();
        this.guestDiskMapper.insert(guestDisk);
        GuestNetworkEntity guestNetwork;
        guestNetwork = this.allocateService.allocateNetwork(network.getNetworkId());
        guestNetwork.setDeviceId(0);
        guestNetwork.setDriveType(configService.getConfig(Constant.ConfigKey.SYSTEM_COMPONENT_NETWORK_DRIVER));
        guestNetwork.setAllocateId(guest.getGuestId());
        guestNetwork.setAllocateType(Constant.NetworkAllocateType.GUEST);
        this.guestNetworkMapper.updateById(guestNetwork);
        guest.setGuestIp(guestNetwork.getIp());
        this.guestMapper.updateById(guest);
        if (Objects.equals(network.getType(), Constant.NetworkType.VLAN)) {
            //调整基础网络网卡为第一网卡
            guestNetwork.setDeviceId(1);
            this.guestNetworkMapper.updateById(guestNetwork);

            GuestNetworkEntity basicGuestNetwork = this.allocateService.allocateNetwork(network.getBasicNetworkId());
            basicGuestNetwork.setDeviceId(0);
            basicGuestNetwork.setDriveType(configService.getConfig(Constant.ConfigKey.SYSTEM_COMPONENT_NETWORK_DRIVER));
            basicGuestNetwork.setAllocateId(guest.getGuestId());
            basicGuestNetwork.setAllocateType(Constant.NetworkAllocateType.GUEST);
            this.guestNetworkMapper.updateById(basicGuestNetwork);
        }
        BaseOperateParam operateParam = CreateGuestOperate.builder()
                .guestId(guest.getGuestId())
                .templateId(diskTemplateId)
                .volumeId(volume.getVolumeId())
                .start(true)
                .hostId(0)
                .id(uid)
                .title("创建系统主机[" + guest.getDescription() + "]")
                .build();
        this.operateTask.addTask(operateParam);
        return guest;
    }

    @Override
    public boolean supports(@NonNull Integer type) {
        return Objects.equals(type, this.getComponentType());
    }

    /**
     * 获取组件类型
     *
     * @return
     */
    public abstract int getComponentType();

    /**
     * 获取组件名称
     *
     * @return
     */
    public abstract String getComponentName();

    @Override
    public GuestQmaRequest getStartQmaRequest(ComponentEntity component, int guestId, Map<String, Object> sysconfig) {
        int qmaExecuteExpire = this.configService.getConfig(Constant.ConfigKey.SYSTEM_COMPONENT_QMA_EXECUTE_TIMEOUT_MINUTES);
        int qmaCheckExpire = this.configService.getConfig(Constant.ConfigKey.SYSTEM_COMPONENT_QMA_CHECK_TIMEOUT_MINUTES);
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        GuestQmaRequest request = GuestQmaRequest.builder().build();
        request.setName("");
        request.setQmaExecuteTimeout((int) TimeUnit.MINUTES.toSeconds(qmaExecuteExpire));
        request.setQmaCheckTimeout((int) TimeUnit.MINUTES.toSeconds(qmaCheckExpire));
        request.setCommands(commands);
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("hostnamectl").args(new String[]{"set-hostname", this.getComponentName()}).checkSuccess(true).build())).build());
        for (ComponentQmaInitialize componentQmaInitialize : componentQmaInitializeList) {
            List<GuestQmaRequest.QmaBody> childCommands = componentQmaInitialize.initialize(component, guestId, sysconfig);
            if (!ObjectUtils.isEmpty(childCommands)) {
                commands.addAll(childCommands);
            }
        }
        return request;
    }
}
