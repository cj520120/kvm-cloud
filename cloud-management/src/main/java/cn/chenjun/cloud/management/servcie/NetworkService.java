package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.dao.ComponentDao;
import cn.chenjun.cloud.management.data.dao.ComponentGuestDao;
import cn.chenjun.cloud.management.data.dao.DnsDao;
import cn.chenjun.cloud.management.data.dao.NatDao;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NatEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.bean.CreateNetworkOperate;
import cn.chenjun.cloud.management.operate.bean.DestroyNetworkOperate;
import cn.chenjun.cloud.management.util.IpCalculate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class NetworkService extends AbstractService {
    @Autowired
    private DnsDao dnsMapper;
    @Autowired
    private ComponentDao componentMapper;
    @Autowired
    private ComponentGuestDao componentGuestMapper;
    @Autowired
    private AllocateService allocateService;
    @Autowired
    private NatDao natMapper;

    public List<GuestNetworkEntity> listGuestNetworks(int guestId) {
        List<GuestNetworkEntity> networkList = guestNetworkDao.listByGuestId(guestId);
        networkList.sort(Comparator.comparingInt(GuestNetworkEntity::getDeviceId));

        return networkList;
    }

    public NetworkEntity getNetworkInfo(int networkId) {
        NetworkEntity network = this.networkDao.findById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        return network;
    }

    public Page<NetworkEntity> search(String keyword, int no, int size) {
        Page<NetworkEntity> page = this.networkDao.search(keyword, no, size);
//        return ResultUtil.success(Page.convert(page, source -> BeanConverter.convert(source, SimpleNetworkModel.class)));
        return page;
    }

    public List<NetworkEntity> listNetwork() {
        List<NetworkEntity> networkList = this.networkDao.listAll();
//        List<SimpleNetworkModel> models = BeanConverter.convert(networkList, SimpleNetworkModel.class);
        return networkList;
    }

    @Transactional(rollbackFor = Exception.class)
    public NetworkEntity createNetwork(String name, String startIp, String endIp, String gateway, String mask, String subnet, String broadcast, String bridge, String dns, String domain, int type, int vlanId, int basicNetworkId, int bridgeType) {

        if (Objects.equals(cn.chenjun.cloud.common.util.Constant.NetworkType.VLAN, type)) {
            NetworkEntity basicNetwork = this.networkDao.findById(basicNetworkId);
            if (basicNetwork == null) {
                throw new CodeException(ErrorCode.PARAM_ERROR, "基础网络不存在");
            }
            bridge = basicNetwork.getBridge();
            if (!Objects.equals(basicNetwork.getBridgeType(), cn.chenjun.cloud.common.util.Constant.NetworkBridgeType.OPEN_SWITCH.bridgeType())) {
                throw new CodeException(ErrorCode.PARAM_ERROR, "vlan所属基础网络必须未OpenSwitch网络类型");
            }
            bridgeType = basicNetwork.getBridgeType();
        }
        NetworkEntity network = NetworkEntity.builder()
                .name(name)
                .poolId(DigestUtil.md5Hex(UUID.randomUUID().toString()))
                .startIp(startIp)
                .endIp(endIp)
                .bridgeType(bridgeType)
                .gateway(gateway)
                .mask(mask)
                .subnet(subnet)
                .broadcast(broadcast)
                .bridge(bridge)
                .dns(dns)
                .domain(domain)
                .type(type)
                .vlanId(vlanId)
                .basicNetworkId(basicNetworkId)
                .secret(UUID.randomUUID().toString().replace("-", ""))
                .status(cn.chenjun.cloud.common.util.Constant.NetworkStatus.CREATING)
                .createTime(new Date()).build();
        networkDao.insert(network);
        List<String> ips = IpCalculate.parseIpRange(startIp, endIp);
        for (String ip : ips) {
            GuestNetworkEntity guestNetwork = GuestNetworkEntity.builder()
                    .allocateId(0)
                    .allocateType(Constant.NetworkAllocateType.DEFAULT)
                    .allocateDescription("")
                    .ip(ip)
                    .networkId(network.getNetworkId())
                    .mac(IpCalculate.getRandomMacAddress())
                    .deviceType("")
                    .deviceId(0)
                    .createTime(new Date())
                    .build();
            this.guestNetworkDao.insert(guestNetwork);
        }
        //申请 route 组件
        {
            ComponentEntity component = ComponentEntity.builder()
                    .componentType(cn.chenjun.cloud.common.util.Constant.ComponentType.ROUTE).networkId(network.getNetworkId())
                    .componentVip("").basicComponentVip("")
                    .createTime(new Date()).build();
            componentMapper.insert(component);
            GuestNetworkEntity componentVip = this.allocateService.allocateNetwork(network.getNetworkId(), component.getComponentId(), Constant.NetworkAllocateType.COMPONENT_VIP, 0, Constant.NetworkDriver.VIRTIO, "Route Basic VIP[" + network.getName() + "]");
            component.setComponentVip(componentVip.getIp());
            component.setBasicComponentVip(componentVip.getIp());
            if (type == cn.chenjun.cloud.common.util.Constant.NetworkType.VLAN) {
                GuestNetworkEntity basicComponentVip = this.allocateService.allocateNetwork(basicNetworkId, component.getComponentId(), Constant.NetworkAllocateType.COMPONENT_VIP, 0, Constant.NetworkDriver.VIRTIO, "Route Basic VIP[" + network.getName() + "]");
                component.setBasicComponentVip(basicComponentVip.getIp());
            }
            componentMapper.update(component);
            if (type == cn.chenjun.cloud.common.util.Constant.NetworkType.VLAN && ObjectUtils.isEmpty(gateway)) {
                //如果没有硬件网关地址，则将VIP设置为模拟网关
                network.setGateway(componentVip.getIp());
                this.networkDao.update(network);
            }
        }
        BaseOperateParam operateParam = CreateNetworkOperate.builder().id(UUID.randomUUID().toString()).title("创建网络[" + network.getName() + "]").networkId(network.getNetworkId()).build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return network;
    }

    @Transactional(rollbackFor = Exception.class)
    public NetworkEntity registerNetwork(int networkId) {
        NetworkEntity network = this.networkDao.findById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        network.setStatus(cn.chenjun.cloud.common.util.Constant.NetworkStatus.CREATING);
        this.networkDao.update(network);
        BaseOperateParam operateParam = CreateNetworkOperate.builder().id(UUID.randomUUID().toString()).title("注册网络[" + network.getName() + "]").networkId(network.getNetworkId()).build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return network;
    }

    @Transactional(rollbackFor = Exception.class)
    public NetworkEntity maintenanceNetwork(int networkId) {
        NetworkEntity network = this.networkDao.findById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        network.setStatus(cn.chenjun.cloud.common.util.Constant.NetworkStatus.MAINTENANCE);
        this.networkDao.update(network);
        this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return network;
    }

    @Transactional(rollbackFor = Exception.class)
    public NetworkEntity destroyNetwork(int networkId) {
        NetworkEntity network = this.networkDao.findById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }

        if (guestNetworkDao.countByAllocateGuest(networkId) > 0) {
            throw new CodeException(ErrorCode.NETWORK_HAS_VM, "当前网络被其他虚拟机引用，请首先删除虚拟机");
        }
        network.setStatus(cn.chenjun.cloud.common.util.Constant.NetworkStatus.DESTROY);
        this.networkDao.update(network);
        this.dnsMapper.deleteByNetworkId(networkId);
        this.guestNetworkDao.deleteByNetworkId(networkId);
        List<ComponentEntity> componentList = this.componentMapper.listByNetworkId(networkId);
        for (ComponentEntity componentEntity : componentList) {
            //取消vip地址
            List<GuestNetworkEntity> guestNetworkEntityList = this.guestNetworkDao.listByAllocate(cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.COMPONENT_VIP, componentEntity.getComponentId());
            for (GuestNetworkEntity guestNetwork : guestNetworkEntityList) {
                this.allocateService.releaseNetwork(guestNetwork.getGuestNetworkId());
            }
            this.componentMapper.deleteById(componentEntity.getComponentId());
            this.natMapper.deleteByComponentId(componentEntity.getComponentId());
        }
        BaseOperateParam operateParam = DestroyNetworkOperate.builder().id(UUID.randomUUID().toString()).title("销毁网络[" + network.getName() + "]").networkId(networkId).build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return network;
    }

    public List<ComponentEntity> listNetworkComponent(int networkId) {
        return this.componentMapper.listByNetworkId(networkId);
    }

    @Transactional(rollbackFor = Exception.class)
    public ComponentEntity createComponent(int networkId, int type) {
        NetworkEntity network = this.networkDao.findById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        ComponentEntity component = ComponentEntity.builder()
                .componentType(type).networkId(network.getNetworkId())
                .componentVip("")
                .basicComponentVip("")
                .createTime(new Date()).build();
        componentMapper.insert(component);
        GuestNetworkEntity componentVip = this.allocateService.allocateNetwork(network.getNetworkId(), component.getComponentId(), Constant.NetworkAllocateType.COMPONENT_VIP, 0, Constant.NetworkDriver.VIRTIO, "Nat VIP [" + network.getName() + "]");
        component.setComponentVip(componentVip.getIp());
        component.setBasicComponentVip(componentVip.getIp());
        if (Objects.equals(network.getType(), Constant.NetworkType.VLAN)) {
            GuestNetworkEntity basicComponentVip = this.allocateService.allocateNetwork(network.getBasicNetworkId(), component.getComponentId(), Constant.NetworkAllocateType.COMPONENT_VIP, 0, Constant.NetworkDriver.VIRTIO, "Nat VIP [" + network.getName() + "]");
            component.setBasicComponentVip(basicComponentVip.getIp());
        }
        componentMapper.update(component);
        this.notifyService.publish(NotifyData.<Void>builder().id(component.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT).build());
        return component;
    }

    @Transactional(rollbackFor = Exception.class)
    public void destroyComponent(int componentId) {
        ComponentEntity component = this.componentMapper.findById(componentId);
        if (component == null) {
            return;
        }
        List<GuestNetworkEntity> list = this.guestNetworkDao.listByAllocate(cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.COMPONENT_VIP, component.getComponentId());
        for (GuestNetworkEntity guestNetwork : list) {
            this.allocateService.releaseNetwork(guestNetwork.getGuestNetworkId());
        }
        this.componentMapper.deleteById(component.getComponentId());
        this.componentGuestMapper.deleteByComponentId(componentId);
        this.natMapper.deleteByComponentId(componentId);

        this.notifyService.publish(NotifyData.<Void>builder().id(component.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT).build());

    }

    public List<NatEntity> listComponentNat(int componentId) {
        List<NatEntity> entityList = this.natMapper.listByComponentId(componentId);

        return entityList;

    }

    public Page<NatEntity> searchComponentNat(String keyword, int componentId, int no, int size) {
        Page<NatEntity> page = this.natMapper.search(keyword, componentId, no, size);
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    public NatEntity createComponentNat(int componentId, int localPort, String protocol, String remoteIp, int remotePort) {
        ComponentEntity component = this.componentMapper.findById(componentId);
        if (component == null) {
            throw new CodeException(ErrorCode.NETWORK_COMPONENT_NOT_FOUND, "网络组件不存在");
        }
        NatEntity entity = NatEntity.builder().componentId(componentId).localPort(localPort).protocol(protocol).remotePort(remotePort).remoteIp(remoteIp).createTime(new Date()).build();
        this.natMapper.insert(entity);
        this.notifyService.publish(NotifyData.<Void>builder().id(entity.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT_NAT).build());
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteComponentNat(int natId) {
        NatEntity entity = this.natMapper.findById(natId);
        if (entity == null) {
            return;
        }
        this.natMapper.deleteById(natId);
        this.notifyService.publish(NotifyData.<Void>builder().id(entity.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT_NAT).build());

    }

    public List<GuestNetworkEntity> listNetworkNic(int networkId) {
        List<GuestNetworkEntity> list = this.guestNetworkDao.listByNetworkId(networkId);
//        List<NicMode> nicModes = list.stream().map(this::initNicModel).collect(Collectors.toList());
//        return ResultUtil.success(nicModes);
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public GuestNetworkEntity allocateSpecialNetworkNic(int guestNetworkId, int allocateId, String allocateDescription) {
        if (guestNetworkId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "参数错误");
        }
        GuestNetworkEntity guestNetwork = this.guestNetworkDao.findById(guestNetworkId);
        if(guestNetwork==null){
            throw new CodeException(ErrorCode.PARAM_ERROR, "参数错误");
        }
        if(guestNetwork.getAllocateType()!=Constant.NetworkAllocateType.DEFAULT){
            throw new CodeException(ErrorCode.PARAM_ERROR, "该地址已经被分配");
        }
        guestNetwork.setAllocateType(Constant.NetworkAllocateType.CUSTOM);
        guestNetwork.setAllocateId(allocateId);
        guestNetwork.setAllocateDescription(allocateDescription);
        this.guestNetworkDao.update(guestNetwork);
        return guestNetwork;
    }

    @Transactional(rollbackFor = Exception.class)
    public GuestNetworkEntity releaseSpecialNetworkNic(int guestNetworkId) {
        GuestNetworkEntity guestNetwork = this.guestNetworkDao.findById(guestNetworkId);
        if (guestNetwork == null) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "参数错误");
        }
        if (guestNetwork.getAllocateType() != Constant.NetworkAllocateType.CUSTOM) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "只允许释放自定义的网络分配");
        }
        guestNetwork = this.allocateService.releaseNetwork(guestNetwork.getGuestNetworkId());
        return guestNetwork;
    }


    public List<NetworkEntity> listNetworkByIds(List<Integer> networkIds) {
        return this.networkDao.listNetworkByIds(networkIds);
    }
}
