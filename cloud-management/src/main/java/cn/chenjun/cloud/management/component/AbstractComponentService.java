package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.bean.NotifyInfo;
import cn.chenjun.cloud.management.annotation.Lock;
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
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
     * @param networkId
     */
    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public void checkAndStart(int networkId) {
        NetworkEntity network = networkMapper.selectById(networkId);
        if (network == null) {
            return;
        }
        if (!Objects.equals(network.getStatus(), Constant.NetworkStatus.READY)) {
            return;
        }
        List<ComponentEntity> componentList = componentMapper.selectList(new QueryWrapper<ComponentEntity>().eq("network_id", networkId).eq("component_type", this.getComponentType()));
        while (componentList.size() > 1) {
            for (ComponentEntity component : componentList) {
                guestService.destroyGuest(component.getGuestId());
                componentMapper.deleteById(component.getComponentId());
            }
        }
        if (!componentList.isEmpty()) {
            ComponentEntity component = componentList.get(0);
            GuestEntity guest = this.guestMapper.selectById(component.getGuestId());
            if (guest == null) {
                componentMapper.deleteById(component.getComponentId());
                return;
            }
            switch (guest.getStatus()) {
                case Constant.GuestStatus.STOP:
                    guest.setStatus(Constant.GuestStatus.STARTING);
                    guestMapper.updateById(guest);
                    HostEntity host = this.allocateService.allocateHost(guest.getLastHostId(), 0, guest.getCpu(), guest.getMemory());
                    this.componentMapper.updateById(component);
                    BaseOperateParam operateParam = StartComponentGuestOperate.builder().taskId(UUID.randomUUID().toString()).title("启动系统主机[" + this.getComponentName() + "]").guestId(guest.getGuestId()).hostId(host.getHostId()).build();
                    this.operateTask.addTask(operateParam);
                    this.notifyService.publish(NotifyInfo.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                    break;
                case Constant.GuestStatus.ERROR:
                    this.guestService.destroyGuest(guest.getGuestId());
                    break;
                default:
                    break;
            }
        } else {
            List<TemplateEntity> templateList = this.templateMapper.selectList(new QueryWrapper<TemplateEntity>().eq("template_type", Constant.TemplateType.SYSTEM).eq("template_status", Constant.TemplateStatus.READY));
            if (templateList.isEmpty()) {
                log.info("系统模版未就绪，等待就绪后创建系统组件.networkId={}",networkId);
                return;
            }
            Collections.shuffle(templateList);
            int templateId = templateList.get(0).getTemplateId();
            GuestEntity guest = createSystemComponentGuest( network,templateId);
            this.notifyService.publish(NotifyInfo.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
        }
    }

    private GuestEntity createSystemComponentGuest(NetworkEntity network,int diskTemplateId) {
        String uid = UUID.randomUUID().toString().replace("-", "");
        GuestEntity guest = GuestEntity.builder()
                .name(GuestNameUtil.getName())
                .description(this.getComponentName())
                .busType(cn.chenjun.cloud.common.util.Constant.DiskBus.VIRTIO)
                .cpu(applicationConfig.getSystemComponentCpu())
                .speed(applicationConfig.getSystemComponentCpuSpeed())
                .memory(applicationConfig.getSystemComponentMemory())
                .cdRoom(0)
                .hostId(0)
                .lastHostId(0)
                .schemeId(0)
                .guestIp("")
                .networkId(network.getNetworkId())
                .type(Constant.GuestType.SYSTEM)
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
        guestNetwork.setGuestId(guest.getGuestId());
        this.guestNetworkMapper.updateById(guestNetwork);
        guest.setGuestIp(guestNetwork.getIp());
        this.guestMapper.updateById(guest);
        if (network.getBasicNetworkId() > 0 && this.allocateBasicNic()) {
            guestNetwork = this.allocateService.allocateNetwork(network.getBasicNetworkId());
            guestNetwork.setDeviceId(networkDeviceId);
            guestNetwork.setDriveType(applicationConfig.getSystemComponentNetworkDriver());
            guestNetwork.setGuestId(guest.getGuestId());
            this.guestNetworkMapper.updateById(guestNetwork);
        }
        componentMapper.insert(ComponentEntity.builder().guestId(guest.getGuestId()).componentType(this.getComponentType()).networkId(network.getNetworkId()).build());
        BaseOperateParam operateParam = CreateGuestOperate.builder()
                .guestId(guest.getGuestId())
                .snapshotVolumeId(0)
                .templateId(diskTemplateId)
                .volumeId(volume.getVolumeId())
                .start(true)
                .hostId(0)
                .taskId(uid)
                .title("创建系统主机[" + this.getComponentName() + "]")
                .build();
        this.operateTask.addTask(operateParam);
        return guest;
    }

    protected String getNicConfig(int index, String ip, String netmask, String gateway, String dns) {
        StringBuilder sb = new StringBuilder();
        sb.append("TYPE=Ethernet").append("\r\n");
        sb.append("BROWSER_ONLY=no").append("\r\n");
        sb.append("BOOTPROTO=static").append("\r\n");
        sb.append("DEFROUTE=yes").append("\r\n");
        sb.append("IPV4_FAILURE_FATAL=no").append("\r\n");
        sb.append("NAME=eth").append(index).append("\r\n");
        sb.append("DEVICE=eth").append(index).append("\r\n");
        sb.append("ONBOOT=yes").append("\r\n");
        if (!StringUtils.isEmpty(ip)) {
            sb.append("IPADDR=").append(ip).append("\r\n");
        }
        if (!StringUtils.isEmpty(netmask)) {
            sb.append("NETMASK=").append(netmask).append("\r\n");
        }
        if (!StringUtils.isEmpty(gateway)) {
            sb.append("GATEWAY=").append(gateway).append("\r\n");
        }
        if (!StringUtils.isEmpty(dns)) {
            for (String s : dns.split(",")) {
                String dnsStr = s.trim();
                if (!StringUtils.isEmpty(dnsStr)) {
                    sb.append("DNS1=").append(dnsStr).append("\r\n");
                }
            }
        }
        return sb.toString();
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
     * 获取组件启动脚本
     *
     * @param guestId
     * @return
     */
    public abstract GuestQmaRequest getStartQmaRequest(int guestId);

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
