package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.config.ApplicationConfig;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.CreateGuestOperate;
import cn.chenjun.cloud.management.operate.bean.StartComponentGuestOperate;
import cn.chenjun.cloud.management.servcie.AbstractService;
import cn.chenjun.cloud.management.servcie.AllocateService;
import cn.chenjun.cloud.management.servcie.GuestService;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.GuestNameUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author chenjun
 */
@Slf4j
public abstract class AbstractComponentService extends AbstractService {

    @Autowired
    protected AllocateService allocateService;
    @Autowired
    protected ComponentMapper componentMapper;
    @Autowired
    protected GuestService guestService;

    @Autowired
    protected ApplicationConfig applicationConfig;

    /**
     * 创建系统组件
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkAndStart(int networkId) {
        NetworkEntity network = networkMapper.selectById(networkId);
        if (network == null) {
            return;
        }
        if (!Objects.equals(network.getStatus(), Constant.NetworkStatus.READY)) {
            return;
        }
        ComponentEntity component = checkAndAllocateComponent(network);
        GuestEntity masterGuest = checkAndStartMasterComponent(networkId, component, network);
        if (masterGuest == null) {
            return;
        }
        if (masterGuest.getStatus() == Constant.GuestStatus.RUNNING) {
            if (!checkComponentSlaveNumber(component)) {
                return;
            }
            checkAndStartSlaveComponent(component, network);
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
        while (slaveList.size() > component.getComponentSlaveNumber()) {
            //删除多余的slave组件
            int slaveGuestId = slaveList.remove(slaveList.size() - 1);
            if (slaveGuestId > 0) {
                GuestEntity slaveGuest = this.guestMapper.selectById(slaveGuestId);
                if (slaveGuest != null) {
                    this.destroySlaveGuest(slaveGuest);
                    //slave销毁成功后再进行后续操作
                    return false;
                }
            }
        }
        return true;
    }

    private void checkAndStartSlaveComponent(ComponentEntity component, NetworkEntity network) {
        List<Integer> slaveList = GsonBuilderUtil.create().fromJson(component.getSlaveGuestIds(), new TypeToken<List<Integer>>() {
        }.getType());
        boolean isUpdateSlave = false;
        int templateId = 0;
        for (int i = 0; i < slaveList.size() && templateId >= 0; i++) {
            int slaveGuestId = slaveList.get(i);
            GuestEntity slaveGuest;
            if (slaveGuestId == 0 || (slaveGuest = this.guestMapper.selectById(slaveGuestId)) == null) {
                //创建已经删除或无效的slave组件
                if (templateId == 0) {
                    templateId = getTemplateId(component.getNetworkId());
                }
                slaveGuest = this.createSystemComponentGuest(component.getComponentId(), "Slave " + this.getComponentName(), network, templateId);
                slaveList.set(i, slaveGuest.getGuestId());
                isUpdateSlave = true;
                this.eventService.publish(NotifyData.<Void>builder().id(slaveGuest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());

            }
            this.startComponentGuest(slaveGuest);
        }
        if (isUpdateSlave) {
            component.setSlaveGuestIds(GsonBuilderUtil.create().toJson(slaveList));
            this.componentMapper.updateById(component);
        }
    }

    private GuestEntity checkAndStartMasterComponent(int networkId, ComponentEntity component, NetworkEntity network) {
        GuestEntity masterGuest = this.guestMapper.selectById(component.getMasterGuestId());
        if (masterGuest == null) {
            //创建master节点
            int templateId = getTemplateId(networkId);
            if (templateId < 0) {
                return null;
            }
            masterGuest = createSystemComponentGuest(component.getComponentId(), "Master " + this.getComponentName(), network, templateId);
            this.eventService.publish(NotifyData.<Void>builder().id(masterGuest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
            component.setMasterGuestId(masterGuest.getGuestId());
            this.componentMapper.updateById(component);
        }
        this.startComponentGuest(masterGuest);
        return masterGuest;
    }

    private ComponentEntity checkAndAllocateComponent(NetworkEntity network) {
        ComponentEntity component = componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.NETWORK_ID, network.getNetworkId()).eq(ComponentEntity.COMPONENT_TYPE, this.getComponentType()));
        if (component == null) {
            component = ComponentEntity.builder().masterGuestId(0).componentSlaveNumber(this.applicationConfig.getSystemComponentSlaveNumber()).slaveGuestIds("[]").componentType(this.getComponentType()).networkId(network.getNetworkId()).componentVip("").build();
            componentMapper.insert(component);
        }
        if (ObjectUtils.isEmpty(component.getComponentVip())) {
            //申请VIP地址
            GuestNetworkEntity guestNetwork = this.guestNetworkMapper.selectOne(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_ID, network.getNetworkId()).eq(GuestNetworkEntity.ALLOCATE_TYPE, this.getVipAddressAllocateType()).ne("allocate_id", 0).last("limit 0,1"));
            if (guestNetwork == null) {
                guestNetwork = this.allocateService.allocateNetwork(network.getNetworkId());
                guestNetwork.setAllocateId(network.getNetworkId());
                guestNetwork.setAllocateType(this.getVipAddressAllocateType());
                this.guestNetworkMapper.updateById(guestNetwork);
            }
            component.setComponentVip(guestNetwork.getIp());
            componentMapper.updateById(component);
            this.eventService.publish(NotifyData.<Void>builder().type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT).id(component.getComponentId()).build());

        }
        return component;
    }

    private int getTemplateId(int networkId) {
        int templateId;
        List<TemplateEntity> templateList = this.templateMapper.selectList(new QueryWrapper<TemplateEntity>().eq(TemplateEntity.TEMPLATE_TYPE, Constant.TemplateType.SYSTEM).eq(TemplateEntity.TEMPLATE_STATUS, Constant.TemplateStatus.READY));
        if (templateList.isEmpty()) {
            log.info("系统模版未就绪，等待就绪后创建系统组件.networkId={}", networkId);
            templateId = -1;
        } else {
            Collections.shuffle(templateList);
            templateId = templateList.get(0).getTemplateId();
        }
        return templateId;
    }

    private void startComponentGuest(GuestEntity masterGuest) {
        switch (masterGuest.getStatus()) {
            case Constant.GuestStatus.STOP:
                masterGuest.setStatus(Constant.GuestStatus.STARTING);
                guestMapper.updateById(masterGuest);
                HostEntity host = this.allocateService.allocateHost(masterGuest.getLastHostId(), 0, masterGuest.getCpu(), masterGuest.getMemory());
                BaseOperateParam operateParam = StartComponentGuestOperate.builder().taskId(UUID.randomUUID().toString()).title("启动系统主机[" + this.getComponentName() + "]").guestId(masterGuest.getGuestId()).hostId(host.getHostId()).componentType(this.getComponentType()).build();
                this.operateTask.addTask(operateParam);
                this.eventService.publish(NotifyData.<Void>builder().id(masterGuest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                break;
            case Constant.GuestStatus.ERROR:
                this.guestService.destroyGuest(masterGuest.getGuestId());
                break;
            default:
                break;
        }
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
        GuestEntity guest = GuestEntity.builder()
                .name(GuestNameUtil.getName())
                .groupId(0)
                .description(name)
                .busType(cn.chenjun.cloud.common.util.Constant.DiskBus.VIRTIO)
                .cpu(applicationConfig.getSystemComponentCpu())
                .speed(applicationConfig.getSystemComponentCpuSpeed())
                .memory(applicationConfig.getSystemComponentMemory())
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
        StorageEntity storage = this.allocateService.allocateStorage(0);
        VolumeEntity volume = VolumeEntity.builder()
                .description("ROOT-" + guest.getGuestId())
                .capacity(0L)
                .storageId(storage.getStorageId())
                .name(uid)
                .path(storage.getMountPath() + "/" + uid)
                .type(cn.chenjun.cloud.common.util.Constant.VolumeType.QCOW2)
                .backingPath("")
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
        int networkDeviceId = 0;
        guestNetwork = this.allocateService.allocateNetwork(network.getNetworkId());
        guestNetwork.setDeviceId(networkDeviceId++);
        guestNetwork.setDriveType(this.applicationConfig.getSystemComponentNetworkDriver());
        guestNetwork.setAllocateId(guest.getGuestId());
        guestNetwork.setAllocateType(Constant.NetworkAllocateType.GUEST);
        this.guestNetworkMapper.updateById(guestNetwork);
        guest.setGuestIp(guestNetwork.getIp());
        this.guestMapper.updateById(guest);
        if (network.getBasicNetworkId() > 0 && this.allocateBasicNic()) {
            //只有master节点需要基础网络
            guestNetwork = this.allocateService.allocateNetwork(network.getBasicNetworkId());
            guestNetwork.setDeviceId(networkDeviceId);
            guestNetwork.setDriveType(applicationConfig.getSystemComponentNetworkDriver());
            guestNetwork.setAllocateId(guest.getGuestId());
            guestNetwork.setAllocateType(Constant.NetworkAllocateType.GUEST);
            this.guestNetworkMapper.updateById(guestNetwork);
        }
        BaseOperateParam operateParam = CreateGuestOperate.builder()
                .guestId(guest.getGuestId())
                .snapshotVolumeId(0)
                .templateId(diskTemplateId)
                .volumeId(volume.getVolumeId())
                .start(true)
                .hostId(0)
                .taskId(uid)
                .title("创建系统主机[" + guest.getDescription() + "]")
                .build();
        this.operateTask.addTask(operateParam);
        return guest;
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

    /**
     * 获取vip地址类型
     *
     * @return
     */
    public abstract int getVipAddressAllocateType();
    /**
     * 获取组件启动脚本
     * @param networkId
     * @param guestId
     * @return
     */
    public abstract GuestQmaRequest getStartQmaRequest(int networkId, int guestId);

    /**
     * 是否申请父网卡ip
     *
     * @return
     */
    public abstract boolean allocateBasicNic();

    /**
     * 之行顺序
     *
     * @return
     */
    public abstract int order();
}
