package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.operate.bean.CreateGuestOperate;
import cn.chenjun.cloud.management.operate.bean.StartComponentGuestOperate;
import cn.chenjun.cloud.management.servcie.AbstractService;
import cn.chenjun.cloud.management.servcie.AllocateService;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.servcie.GuestService;
import cn.chenjun.cloud.management.servcie.bean.GuestExtern;
import cn.chenjun.cloud.management.util.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
public abstract class AbstractComponentService<T extends ComponentQmaInitialize> extends AbstractService implements ComponentProcess {

    private final List<T> componentQmaInitializeList;
    @Autowired
    protected AllocateService allocateService;
    @Autowired
    protected ComponentMapper componentMapper;
    @Autowired
    protected GuestService guestService;
    @Autowired
    protected ConfigService configService;

    protected AbstractComponentService(List<T> componentQmaInitializeList) {
        componentQmaInitializeList.sort(Comparator.comparingInt(Ordered::getOrder));
        this.componentQmaInitializeList = componentQmaInitializeList;
    }

    @Override
    public void cleanHostComponent(ComponentEntity component, HostEntity host) {
        QueryWrapper<GuestEntity> wrapper=new QueryWrapper<GuestEntity>().eq(GuestEntity.BIND_HOST_ID, host.getHostId()).eq(GuestEntity.OTHER_ID, component.getComponentId());
        List<GuestEntity> guestList = this.guestMapper.selectList(wrapper);
        for (GuestEntity guest : guestList) {
            this.destroyGuest(guest);
        }
    }
    /**
     * 创建系统组件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndStart(NetworkEntity network, ComponentEntity component, HostEntity host, boolean isMaster) {
        QueryWrapper<GuestEntity> wrapper=new QueryWrapper<GuestEntity>().eq(GuestEntity.BIND_HOST_ID, host.getHostId()).eq(GuestEntity.OTHER_ID, component.getComponentId());
        List<GuestEntity> guestList=this.guestMapper.selectList(wrapper);
        while(guestList.size()>1) {
            GuestEntity guest=guestList.remove(0);
            this.destroyGuest(guest);
        }

        GuestEntity guest;
        if(guestList.isEmpty()){
            int templateId = getTemplateId();
            if(templateId<0){
                return false;
            }
            guest=this.createSystemComponentGuest(host.getHostId(),component.getComponentId(), isMaster ? "Master " + this.getComponentName() : "Slave " + this.getComponentName(), network, getTemplateId());
        }else{
            guest=guestList.get(0);
        }
        boolean isReady=false;
        switch (guest.getStatus()){
            case Constant.GuestStatus.RUNNING:
                isReady=true;
                break;
            case Constant.GuestStatus.STOP:
                guest.setDescription(isMaster ? "Master " + this.getComponentName() : "Slave " + this.getComponentName());
                guest.setStatus(cn.chenjun.cloud.common.util.Constant.GuestStatus.STARTING);
                guest.setLastHostId(guest.getBindHostId());
                guestMapper.updateById(guest);
                BaseOperateParam operateParam = StartComponentGuestOperate.builder().id(UUID.randomUUID().toString()).title("启动系统主机[" + this.getComponentName() + "]").guestId(guest.getGuestId()).hostId(guest.getBindHostId()).componentType(this.getComponentType()).build();
                this.operateTask.addTask(operateParam);
                break;
            case Constant.GuestStatus.ERROR:
                this.guestService.destroyGuest(guest.getGuestId());
                break;
            default:
                break;
        }
        return isReady;
    }



    private int getTemplateId() {
        int templateId;
        List<TemplateEntity> templateList = this.templateMapper.selectList(new QueryWrapper<TemplateEntity>().eq(TemplateEntity.TEMPLATE_TYPE, cn.chenjun.cloud.common.util.Constant.TemplateType.SYSTEM).eq(TemplateEntity.TEMPLATE_STATUS, cn.chenjun.cloud.common.util.Constant.TemplateStatus.READY));
        if (templateList.isEmpty()) {
            log.warn("系统模版未就绪，等待就绪后创建系统组件.{}", this.getComponentName());
            templateId = -1;
        } else {
            Collections.shuffle(templateList);
            templateId = templateList.get(0).getTemplateId();
        }
        return templateId;
    }

    private void destroyGuest(GuestEntity guest) {
        switch (guest.getStatus()) {
            case cn.chenjun.cloud.common.util.Constant.GuestStatus.STOP:
            case cn.chenjun.cloud.common.util.Constant.GuestStatus.ERROR:
            case Constant.GuestStatus.DESTROY:
                this.guestService.destroyGuest(guest.getGuestId());
                break;
            case cn.chenjun.cloud.common.util.Constant.GuestStatus.RUNNING:
                this.guestService.shutdown(guest.getGuestId(), true);
                break;
            default:
                break;
        }
    }


    private GuestEntity createSystemComponentGuest(int hostId,int componentId, String name, NetworkEntity network, int diskTemplateId) {
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
        List<StorageEntity> storageList = this.storageMapper.selectList(new QueryWrapper<StorageEntity>());
        storageList.removeIf(storage->(storage.getSupportCategory() & Constant.StorageCategory.VOLUME) != Constant.StorageCategory.VOLUME);
        storageList.removeIf(storage->(storage.getStatus() & Constant.StorageStatus.READY) != Constant.StorageStatus.READY);
        storageList.removeIf(storage->!Objects.equals(storage.getHostId(),hostId));

        StorageEntity storage =storageList.stream().findFirst().orElseGet(() -> this.allocateService.allocateStorage(cn.chenjun.cloud.common.util.Constant.StorageCategory.VOLUME, 0));

        GuestEntity guest = GuestEntity.builder()
                .name(NameUtil.generateGuestName())
                .groupId(0)
                .uuid(UUID.randomUUID().toString())
                .description(name)
                .systemCategory(Constant.SystemCategory.CENTOS)
                .bootstrapType(bootStrapType)
                .cpu(systemCpu)
                .share(systemCpuShare)
                .memory(systemMemory)
                .cdRoom(0)
                .bindHostId(hostId)
                .hostId(0)
                .lastHostId(0)
                .schemeId(0)
                .otherId(componentId)
                .guestIp("")
                .networkId(network.getNetworkId())
                .extern("{}")
                .type(cn.chenjun.cloud.common.util.Constant.GuestType.COMPONENT)
                .status(cn.chenjun.cloud.common.util.Constant.GuestStatus.CREATING)
                .build();
        this.guestMapper.insert(guest);
        GuestExtern extern = new GuestExtern();
        extern.setMetaData(GuestExternUtil.buildMetaDataParam(guest, this.getComponentName().replace(" ", "_").toLowerCase()));
        extern.setUserData(GuestExternUtil.buildUserDataParam(guest, "123456", ""));
        extern.setVnc(GuestExternUtil.buildVncParam(guest, "", "5900"));
        guest.setExtern(GsonBuilderUtil.create().toJson(extern));
        this.guestMapper.updateById(guest);
        String volumeType = this.configService.getConfig(ConfigKey.DEFAULT_DISK_TYPE);
        if (Objects.equals(storage.getType(), cn.chenjun.cloud.common.util.Constant.StorageType.CEPH_RBD)) {
            volumeType = cn.chenjun.cloud.common.util.Constant.VolumeType.RAW;
        }
        String volumeName = NameUtil.generateVolumeName();
        VolumeEntity volume = VolumeEntity.builder()
                .description("ROOT-" + guest.getGuestId())
                .capacity(0L)
                .hostId(storage.getHostId())
                .storageId(storage.getStorageId())
                .name(volumeName)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .templateId(diskTemplateId)
                .allocation(0L)
                .capacity(0L)
                .guestId(guest.getGuestId())
                .deviceId(0)
                .deviceDriver(cn.chenjun.cloud.common.util.Constant.DiskDriveType.VIRTIO)
                .status(Constant.VolumeStatus.CREATING)
                .device(Constant.DeviceType.DISK)
                .serial(DiskSerialUtil.generateDiskSerial())
                .build();
        this.volumeMapper.insert(volume);
        String driveType = configService.getConfig(ConfigKey.SYSTEM_COMPONENT_NETWORK_DRIVER);
        boolean isVlan = Objects.equals(network.getType(), cn.chenjun.cloud.common.util.Constant.NetworkType.VLAN);
        int deviceId = isVlan ? 1 : 0;
        GuestNetworkEntity guestNetwork = this.allocateService.allocateNetwork(network.getNetworkId(), guest.getGuestId(), Constant.NetworkAllocateType.GUEST, deviceId, driveType, "Component Guest Basic Nic");
        guest.setGuestIp(guestNetwork.getIp());
        this.guestMapper.updateById(guest);
        if (isVlan) {
            this.allocateService.allocateNetwork(network.getBasicNetworkId(), guest.getGuestId(), Constant.NetworkAllocateType.GUEST, 0, driveType, "Component Guest Basic Nic");
        }
        BaseOperateParam operateParam = CreateGuestOperate.builder()
                .guestId(guest.getGuestId())
                .templateId(diskTemplateId)
                .volumeId(volume.getVolumeId())
                .start(true)
                .hostId(0)
                .id(UUID.randomUUID().toString())
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
        int qmaExecuteExpire = this.configService.getConfig(ConfigKey.SYSTEM_COMPONENT_QMA_EXECUTE_TIMEOUT_MINUTES);
        int qmaCheckExpire = this.configService.getConfig(ConfigKey.SYSTEM_COMPONENT_QMA_CHECK_TIMEOUT_MINUTES);
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
