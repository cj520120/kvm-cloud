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
import cn.chenjun.cloud.management.servcie.bean.SubnetNetwork;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.MacCalculate;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.util.SubnetCalculator;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
        return networkList;
    }

    @Transactional(rollbackFor = Exception.class)
    public NetworkEntity createNetwork(String name, SubnetNetwork subnetNetwork, String bridge, String dns, String domain, int type, int vlanId, int basicNetworkId, int bridgeType) {


        NetworkEntity basicNetwork = null;
        switch (type) {
            case Constant.NetworkType.BASIC:
                break;
            case Constant.NetworkType.VxLAN:
            case Constant.NetworkType.VLAN:
                basicNetwork = this.networkDao.findById(basicNetworkId);
                if (basicNetwork == null) {
                    throw new CodeException(ErrorCode.PARAM_ERROR, "基础网络不存在");
                }
                if (basicNetwork.getStatus() != Constant.NetworkStatus.READY) {
                    throw new CodeException(ErrorCode.PARAM_ERROR, "基础网络必须为就绪状态");
                }
                break;
            default:
                throw new CodeException(ErrorCode.PARAM_ERROR, "不支持的网络类型");
        }
        if (type == Constant.NetworkType.VxLAN) {
            bridge = "";
            bridgeType = Constant.NetworkBridgeType.OVN.bridgeType();
        }
        NetworkEntity network = NetworkEntity.builder()
                .name(name)
                .poolId(DigestUtil.md5Hex(UUID.randomUUID().toString()))
                .startIp(subnetNetwork.getFirstIp())
                .endIp(subnetNetwork.getLastIp())
                .bridgeType(bridgeType)
                .gateway(subnetNetwork.getGateway())
                .mask(subnetNetwork.getMask())
                .subnet(subnetNetwork.getSubnet())
                .broadcast(subnetNetwork.getBroadcast())
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
        List<String> ips = SubnetCalculator.listRangeIps(subnetNetwork.getFirstIp(), subnetNetwork.getLastIp());
        for (String ip : ips) {
            GuestNetworkEntity guestNetwork = GuestNetworkEntity.builder()
                    .allocateId(0)
                    .allocateType(Constant.NetworkAllocateType.DEFAULT)
                    .allocateDescription("")
                    .ip(ip)
                    .networkId(network.getNetworkId())
                    .mac(MacCalculate.getRandomMacAddress())
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
            switch (type) {
                case Constant.NetworkType.BASIC:
                    break;
                case Constant.NetworkType.VxLAN:
                    if (!this.checkOvnSupport()) {
                        throw new CodeException(ErrorCode.PARAM_ERROR, "当前配置不支持VxLan网络");
                    }
                case Constant.NetworkType.VLAN: {
                    GuestNetworkEntity basicComponentVip = this.allocateService.allocateNetwork(basicNetworkId, component.getComponentId(), Constant.NetworkAllocateType.COMPONENT_VIP, 0, Constant.NetworkDriver.VIRTIO, "Route Basic VIP[" + network.getName() + "]");
                    component.setBasicComponentVip(basicComponentVip.getIp());
                }
                break;
                default:
                    throw new CodeException(ErrorCode.PARAM_ERROR, "不支持的网络类型");
            }
            componentMapper.update(component);
        }
        BaseOperateParam operateParam = CreateNetworkOperate.builder().id(UUID.randomUUID().toString()).title("创建网络[" + network.getName() + "]").networkId(network.getNetworkId()).networkType(network.getType()).build();
        this.operateTask.addTask(operateParam);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
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
        BaseOperateParam operateParam = CreateNetworkOperate.builder().id(UUID.randomUUID().toString()).title("注册网络[" + network.getName() + "]").networkId(network.getNetworkId()).networkType(network.getType()).build();
        this.operateTask.addTask(operateParam);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
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
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
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
        BaseOperateParam operateParam = DestroyNetworkOperate.builder().id(UUID.randomUUID().toString()).title("销毁网络[" + network.getName() + "]").networkId(networkId).networkType(network.getType()).build();
        this.operateTask.addTask(operateParam);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
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
        switch (type) {
            case Constant.NetworkType.VLAN:
                GuestNetworkEntity basicComponentVip = this.allocateService.allocateNetwork(network.getBasicNetworkId(), component.getComponentId(), Constant.NetworkAllocateType.COMPONENT_VIP, 0, Constant.NetworkDriver.VIRTIO, "Nat VIP [" + network.getName() + "]");
                component.setBasicComponentVip(basicComponentVip.getIp());
                break;
        }
        componentMapper.update(component);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(component.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT).build());
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

        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(component.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT).build());

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
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(entity.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT_NAT).build());
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteComponentNat(int natId) {
        NatEntity entity = this.natMapper.findById(natId);
        if (entity == null) {
            return;
        }
        this.natMapper.deleteById(natId);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(entity.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT_NAT).build());

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

    public Boolean checkOvnSupport() {
        return Constant.Enable.YES.equals(this.configService.getConfig(ConfigKey.NETWORK_OVN_ENABLE));
    }
}
