package cn.roamblue.cloud.management.servcie;

import cn.roamblue.cloud.common.bean.GuestQmaRequest;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.*;
import cn.roamblue.cloud.management.data.mapper.*;
import cn.roamblue.cloud.management.operate.bean.BaseOperateParam;
import cn.roamblue.cloud.management.operate.bean.CreateGuestOperate;
import cn.roamblue.cloud.management.operate.bean.StartComponentGuestOperate;
import cn.roamblue.cloud.management.task.OperateTask;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
public abstract class ComponentService {
    @Autowired
    protected NetworkMapper networkMapper;
    @Autowired
    protected GuestNetworkMapper guestNetworkMapper;
    @Autowired
    protected StorageMapper storageMapper;
    @Autowired
    protected VolumeMapper volumeMapper;
    @Autowired
    protected AllocateService allocateService;
    @Autowired
    protected TemplateMapper templateMapper;
    @Autowired
    protected ComponentMapper componentMapper;
    @Autowired
    protected GuestService guestService;
    @Autowired
    protected GuestMapper guestMapper;
    @Autowired
    protected GuestDiskMapper guestDiskMapper;
    @Autowired
    protected OperateTask operateTask;

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public void create(int networkId){
        NetworkEntity network = networkMapper.selectById(networkId);
        if(network==null){
            return;
        }
        if(!Objects.equals(network.getStatus(), Constant.NetworkStatus.READY)){
            return;
        }
        List<ComponentEntity> componentList = componentMapper.selectList(new QueryWrapper<ComponentEntity>().eq("network_id", networkId).eq("component_type",this.getComponentType()));
        while (componentList.size() > 1) {
            for (ComponentEntity component : componentList) {
                guestService.destroyGuest(component.getGuestId());
                componentMapper.deleteById(component.getComponentId());
                return;
            }
        }
        if(!componentList.isEmpty()){
            ComponentEntity component=componentList.get(0);
            GuestEntity guest=this.guestMapper.selectById(component.getGuestId());
            if(guest==null){
                componentMapper.deleteById(component.getComponentId());
            }
            else if(Objects.equals(Constant.GuestStatus.STOP,guest.getStatus())){
                //
                guest.setStatus(Constant.GuestStatus.STARTING);
                guestMapper.updateById(guest);
                HostEntity host=this.allocateService.allocateHost(guest.getLastHostId(),0,guest.getCpu(),guest.getMemory());
                BaseOperateParam operateParam= StartComponentGuestOperate.builder().taskId(UUID.randomUUID().toString()).guestId(guest.getGuestId()).hostId(host.getHostId()).build();
                this.operateTask.addTask(operateParam);

            }
        }else{
            List<TemplateEntity> templateList = this.templateMapper.selectList(new QueryWrapper<TemplateEntity>().eq("template_type", Constant.TemplateType.SYSTEM).eq("template_status", Constant.TemplateStatus.READY));
            if (templateList.isEmpty()) {
                return;
            }
            Collections.shuffle(templateList);
            int diskTemplateId = templateList.get(0).getTemplateId();
            String uid = UUID.randomUUID().toString().replace("-", "");
            GuestEntity guest = GuestEntity.builder()
                    .name(uid)
                    .description(this.getComponentName())
                    .busType(cn.roamblue.cloud.common.util.Constant.DiskBus.VIRTIO)
                    .cpu(1)
                    .memory(500*1024L)
                    .cdRoom(0)
                    .hostId(0)
                    .lastHostId(0)
                    .type(Constant.GuestType.SYSTEM)
                    .status(Constant.GuestStatus.CREATING)
                    .build();
            this.guestMapper.insert(guest);
            componentMapper.insert(ComponentEntity.builder().guestId(guest.getGuestId()).componentType(this.getComponentType()).networkId(networkId).build());
            StorageEntity storage=this.allocateService.allocateStorage(0);
            VolumeEntity volume = VolumeEntity.builder()
                    .capacity(0L)
                    .storageId(storage.getStorageId())
                    .name(uid)
                    .path(storage.getMountPath() + "/" + uid)
                    .type(cn.roamblue.cloud.common.util.Constant.VolumeType.QCOW2)
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
            GuestNetworkEntity guestNetwork = this.allocateService.allocateNetwork(networkId);
            guestNetwork.setDeviceId(0);
            guestNetwork.setDriveType(cn.roamblue.cloud.common.util.Constant.NetworkDriver.VIRTIO);
            guestNetwork.setGuestId(guest.getGuestId());
            this.guestNetworkMapper.updateById(guestNetwork);
            if (network.getBasicNetworkId()>0){
                guestNetwork = this.allocateService.allocateNetwork(network.getBasicNetworkId());
                guestNetwork.setDeviceId(1);
                guestNetwork.setDriveType(cn.roamblue.cloud.common.util.Constant.NetworkDriver.VIRTIO);
                guestNetwork.setGuestId(guest.getGuestId());
                this.guestNetworkMapper.updateById(guestNetwork);
            }
            BaseOperateParam operateParam = CreateGuestOperate.builder()
                    .guestId(guest.getGuestId())
                    .snapshotVolumeId(0)
                    .templateId(diskTemplateId)
                    .volumeId(volume.getVolumeId())
                    .start(false)
                    .hostId(0)
                    .taskId(uid)
                    .build();
            this.operateTask.addTask(operateParam);
        }
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

    public abstract int getComponentType();

    public abstract String getComponentName();

    public abstract GuestQmaRequest getQmaRequest(int guestId);
}
