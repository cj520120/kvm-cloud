package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.BeanConverter;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.data.mapper.DnsMapper;
import cn.chenjun.cloud.management.data.mapper.NatMapper;
import cn.chenjun.cloud.management.model.*;
import cn.chenjun.cloud.management.operate.bean.CreateNetworkOperate;
import cn.chenjun.cloud.management.operate.bean.DestroyNetworkOperate;
import cn.chenjun.cloud.management.util.IpCalculate;
import cn.chenjun.cloud.management.util.IpValidator;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class NetworkService extends AbstractService {
    @Autowired
    private DnsMapper dnsMapper;
    @Autowired
    private ComponentMapper componentMapper;
    @Autowired
    private AllocateService allocateService;
    @Autowired
    private NatMapper natMapper;

    public ResultUtil<List<GuestNetworkModel>> listGuestNetworks(int guestId) {
        List<GuestNetworkEntity> networkList = guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.ALLOCATE_ID, guestId).eq(GuestNetworkEntity.ALLOCATE_TYPE, cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.GUEST));
        networkList.sort(Comparator.comparingInt(GuestNetworkEntity::getDeviceId));
        List<GuestNetworkModel> models = networkList.stream().map(this::initNetwork).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    public ResultUtil<NetworkModel> getNetworkInfo(int networkId) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            return ResultUtil.error(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        return ResultUtil.success(this.initNetwork(network));
    }

    public ResultUtil<Page<SimpleNetworkModel>> search(String keyword, int no, int size) {
        QueryWrapper<NetworkEntity> wrapper = new QueryWrapper<>();
        if (!ObjectUtils.isEmpty(keyword)) {
            String condition = "%" + keyword + "%";
            wrapper.like(NetworkEntity.NETWORK_NAME, condition);
        }
        int nCount = Math.toIntExact(this.networkMapper.selectCount(wrapper));
        int nOffset = (no - 1) * size;
        wrapper.last("limit " + nOffset + ", " + size);
        List<NetworkEntity> networkList = this.networkMapper.selectList(wrapper);
        List<SimpleNetworkModel> models = BeanConverter.convert(networkList, SimpleNetworkModel.class);
        Page<SimpleNetworkModel> page = Page.create(nCount, nOffset, size);
        page.setList(models);
        return ResultUtil.success(page);
    }

    public ResultUtil<List<SimpleNetworkModel>> listNetwork() {
        List<NetworkEntity> networkList = this.networkMapper.selectList(new QueryWrapper<>());
        List<SimpleNetworkModel> models = BeanConverter.convert(networkList, SimpleNetworkModel.class);
        return ResultUtil.success(models);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> createNetwork(String name, String startIp, String endIp, String gateway, String mask, String subnet, String broadcast, String bridge, String dns, String domain, int type, int vlanId, int basicNetworkId, int bridgeType) {

        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入网络名称");
        }
        if (!IpValidator.isValidIp(startIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的开始IP");
        }
        if (!IpValidator.isValidIp(endIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的结束IP");
        }
        if (cn.chenjun.cloud.common.util.Constant.NetworkType.BASIC == type && StringUtils.isEmpty(gateway)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的网关地址");
        }
        if (!IpValidator.isValidIp(mask)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的子网掩码");
        }
        if (cn.chenjun.cloud.common.util.Constant.NetworkType.BASIC == type && StringUtils.isEmpty(bridge)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入桥接网卡名称");
        }
        if (StringUtils.isEmpty(dns)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入DNS信息");
        }
        if (!IpValidator.isValidIp(subnet)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的子网信息");
        }
        if (!IpValidator.isValidIp(broadcast)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的广播地址");
        }
        if (StringUtils.isEmpty(domain)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入搜索域");
        }
        if (Objects.equals(cn.chenjun.cloud.common.util.Constant.NetworkType.VLAN, type) && vlanId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入Vlan ID");
        }
        if (Objects.equals(cn.chenjun.cloud.common.util.Constant.NetworkType.VLAN, type)) {
            if (vlanId <= 0) {
                throw new CodeException(ErrorCode.PARAM_ERROR, "请输入Vlan ID");
            }
            if (basicNetworkId <= 0) {
                throw new CodeException(ErrorCode.PARAM_ERROR, "请输入基础网络");
            }
            NetworkEntity basicNetwork = this.networkMapper.selectById(basicNetworkId);
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
        networkMapper.insert(network);
        List<String> ips = IpCalculate.parseIpRange(startIp, endIp);
        for (String ip : ips) {
            GuestNetworkEntity guestNetwork = GuestNetworkEntity.builder()
                    .allocateId(0)
                    .allocateType(cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.GUEST)
                    .ip(ip)
                    .networkId(network.getNetworkId())
                    .mac(IpCalculate.getRandomMacAddress())
                    .driveType("")
                    .deviceId(0)
                    .createTime(new Date())
                    .build();
            this.guestNetworkMapper.insert(guestNetwork);
        }
        //申请 route 组件
        {
            GuestNetworkEntity componentVip = this.allocateService.allocateNetwork(network.getNetworkId());
            GuestNetworkEntity basicComponentVip = componentVip;
            if (type == cn.chenjun.cloud.common.util.Constant.NetworkType.VLAN) {
                basicComponentVip = this.allocateService.allocateNetwork(basicNetworkId);
            }
            ComponentEntity component = ComponentEntity.builder().masterGuestId(0).componentSlaveNumber(0).slaveGuestIds("[]")
                    .componentType(cn.chenjun.cloud.common.util.Constant.ComponentType.ROUTE).networkId(network.getNetworkId())
                    .componentVip(componentVip.getIp()).basicComponentVip(basicComponentVip.getIp())
                    .createTime(new Date()).build();
            componentMapper.insert(component);
            if (type == cn.chenjun.cloud.common.util.Constant.NetworkType.VLAN) {
                basicComponentVip.setDriveType("Vip");
                basicComponentVip.setAllocateId(component.getComponentId());
                basicComponentVip.setAllocateType(cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.COMPONENT_VIP);
                this.guestNetworkMapper.updateById(basicComponentVip);
            }
            componentVip.setDriveType("Vip");
            componentVip.setAllocateId(component.getComponentId());
            componentVip.setAllocateType(cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.COMPONENT_VIP);
            this.guestNetworkMapper.updateById(componentVip);
            if (type == cn.chenjun.cloud.common.util.Constant.NetworkType.VLAN && ObjectUtils.isEmpty(gateway)) {
                //如果没有硬件网关地址，则将VIP设置为模拟网关
                network.setGateway(componentVip.getIp());
                this.networkMapper.updateById(network);
            }
        }
        BaseOperateParam operateParam = CreateNetworkOperate.builder().id(UUID.randomUUID().toString()).title("创建网络[" + network.getName() + "]").networkId(network.getNetworkId()).build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return ResultUtil.success(this.initNetwork(network));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> registerNetwork(int networkId) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        network.setStatus(cn.chenjun.cloud.common.util.Constant.NetworkStatus.CREATING);
        this.networkMapper.updateById(network);
        BaseOperateParam operateParam = CreateNetworkOperate.builder().id(UUID.randomUUID().toString()).title("注册网络[" + network.getName() + "]").networkId(network.getNetworkId()).build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return ResultUtil.success(this.initNetwork(network));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> maintenanceNetwork(int networkId) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        network.setStatus(cn.chenjun.cloud.common.util.Constant.NetworkStatus.MAINTENANCE);
        this.networkMapper.updateById(network);
        this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return ResultUtil.success(this.initNetwork(network));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> destroyNetwork(int networkId) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }

        if (guestNetworkMapper.selectCount(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_ID, networkId).eq(GuestNetworkEntity.ALLOCATE_TYPE, cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.GUEST).ne(GuestNetworkEntity.ALLOCATE_ID, 0)) > 0) {
            throw new CodeException(ErrorCode.NETWORK_HAS_VM, "当前网络被其他虚拟机引用，请首先删除虚拟机");
        }
        network.setStatus(cn.chenjun.cloud.common.util.Constant.NetworkStatus.DESTROY);
        this.networkMapper.updateById(network);
        this.dnsMapper.delete(new QueryWrapper<DnsEntity>().eq(DnsEntity.NETWORK_ID, networkId));
        this.guestNetworkMapper.delete(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_ID, networkId));
        List<ComponentEntity> componentList = this.componentMapper.selectList(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.NETWORK_ID, networkId));
        for (ComponentEntity componentEntity : componentList) {
            //取消vip地址
            List<GuestNetworkEntity> guestNetworkEntityList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.ALLOCATE_ID, componentEntity.getComponentId()).eq(GuestNetworkEntity.ALLOCATE_TYPE, cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.COMPONENT_VIP));
            for (GuestNetworkEntity guestNetwork : guestNetworkEntityList) {
                guestNetwork.setAllocateType(cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.GUEST);
                guestNetwork.setAllocateId(0);
                this.guestNetworkMapper.updateById(guestNetwork);
            }
            this.componentMapper.deleteById(componentEntity);
            this.natMapper.delete(new QueryWrapper<NatEntity>().eq(NatEntity.COMPONENT_ID, componentEntity.getComponentId()));
        }
        BaseOperateParam operateParam = DestroyNetworkOperate.builder().id(UUID.randomUUID().toString()).title("销毁网络[" + network.getName() + "]").networkId(networkId).build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return ResultUtil.success(this.initNetwork(network));
    }

    public ResultUtil<List<ComponentDetailModel>> listNetworkComponent(int networkId) {
        List<ComponentEntity> entityList = this.componentMapper.selectList(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.NETWORK_ID, networkId));
        List<ComponentDetailModel> list = entityList.stream().map(this::initComponent).collect(Collectors.toList());
        return ResultUtil.success(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<ComponentDetailModel> updateComponentSlaveNumber(int componentId, int slaveNumber) {
        ComponentEntity entity = this.componentMapper.selectById(componentId);
        if (entity == null) {
            return ResultUtil.error(ErrorCode.NETWORK_COMPONENT_NOT_FOUND, "网络组件未找到");
        }
        if (slaveNumber < 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "Slave数量必须大于等于0");
        }
        entity.setComponentSlaveNumber(slaveNumber);
        this.componentMapper.updateById(entity);
        this.notifyService.publish(NotifyData.<Void>builder().id(entity.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT).build());
        return ResultUtil.success(this.initComponent(entity));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<ComponentDetailModel> createComponent(int networkId, int type) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        GuestNetworkEntity componentVip = this.allocateService.allocateNetwork(network.getNetworkId());
        GuestNetworkEntity basicComponentVip = componentVip;
        if (Objects.equals(network.getType(), cn.chenjun.cloud.common.util.Constant.NetworkType.VLAN)) {
            basicComponentVip = this.allocateService.allocateNetwork(network.getBasicNetworkId());
        }
        ComponentEntity component = ComponentEntity.builder().masterGuestId(0).componentSlaveNumber(0).slaveGuestIds("[]")
                .componentType(type).networkId(network.getNetworkId())
                .componentVip(componentVip.getIp())
                .basicComponentVip(basicComponentVip.getIp())
                .createTime(new Date()).build();
        componentMapper.insert(component);

        componentVip.setAllocateId(component.getComponentId());
        componentVip.setAllocateType(cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.COMPONENT_VIP);
        this.guestNetworkMapper.updateById(componentVip);
        if (Objects.equals(network.getType(), Constant.NetworkType.VLAN)) {
            basicComponentVip.setAllocateId(component.getComponentId());
            basicComponentVip.setAllocateType(cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.COMPONENT_VIP);
            this.guestNetworkMapper.updateById(basicComponentVip);
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(component.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT).build());
        return ResultUtil.success(this.initComponent(component));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<Void> destroyComponent(int componentId) {

        ComponentEntity component = this.componentMapper.selectById(componentId);
        if (component == null) {
            return ResultUtil.error(ErrorCode.NETWORK_COMPONENT_NOT_FOUND, "网络组件不存在");
        }
        List<Integer> guestIds = GsonBuilderUtil.create().fromJson(component.getSlaveGuestIds(), new TypeToken<List<Integer>>() {
        }.getType());
        guestIds.add(component.getMasterGuestId());
        if (!this.guestMapper.selectBatchIds(guestIds).isEmpty()) {
            return ResultUtil.error(ErrorCode.NETWORK_COMPONENT_HAS_VM, "请删除网络组件对应的虚拟机");
        }
        List<GuestNetworkEntity> list = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.ALLOCATE_ID, component.getComponentId()).eq(GuestNetworkEntity.ALLOCATE_TYPE, cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.COMPONENT_VIP));
        for (GuestNetworkEntity guestNetwork : list) {
            guestNetwork.setAllocateId(0);
            guestNetwork.setAllocateType(cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.GUEST);
            this.guestNetworkMapper.updateById(guestNetwork);
        }
        this.componentMapper.deleteById(component);
        this.natMapper.delete(new QueryWrapper<NatEntity>().eq(NatEntity.COMPONENT_ID, component.getComponentId()));

        this.notifyService.publish(NotifyData.<Void>builder().id(component.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT).build());
        return ResultUtil.<Void>builder().build();
    }

    public ResultUtil<List<NatModel>> listComponentNat(int componentId) {
        List<NatEntity> entityList = this.natMapper.selectList(new QueryWrapper<NatEntity>().eq(NatEntity.COMPONENT_ID, componentId));
        List<NatModel> list = entityList.stream().map(this::initNat).collect(Collectors.toList());
        return ResultUtil.success(list);

    }

    public ResultUtil<Page<NatModel>> searchComponentNat(String keyword, int componentId, int no, int size) {
        QueryWrapper<NatEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(NatEntity.COMPONENT_ID, componentId);
        if (!ObjectUtils.isEmpty(keyword)) {
            queryWrapper.and(o -> {
                String condition = "%" + keyword + "%";
                QueryWrapper<NatEntity> wrapper = o;
                wrapper.like(NatEntity.LOCAL_PORT, condition)
                        .or().like(NatEntity.PROTOCOL, condition)
                        .or().like(NatEntity.REMOTE_IP, condition)
                        .or().like(NatEntity.REMOTE_PORT, condition);
            });
        }
        int nCount = Math.toIntExact(this.natMapper.selectCount(queryWrapper));
        int nOffset = (no - 1) * size;
        queryWrapper.last("limit " + nOffset + ", " + size);
        List<NatEntity> list = this.natMapper.selectList(queryWrapper);
        List<NatModel> models = list.stream().map(this::initNat).collect(Collectors.toList());
        Page<NatModel> page = Page.create(nCount, nOffset, size);
        page.setList(models);
        return ResultUtil.success(page);

    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NatModel> createComponentNat(int componentId, int localPort, String protocol, String remoteIp, int remotePort) {
        ComponentEntity component = this.componentMapper.selectById(componentId);
        if (component == null) {
            return ResultUtil.error(ErrorCode.NETWORK_COMPONENT_NOT_FOUND, "网络组件不存在");
        }
        NatEntity entity = NatEntity.builder().componentId(componentId).localPort(localPort).protocol(protocol).remotePort(remotePort).remoteIp(remoteIp).createTime(new Date()).build();
        this.natMapper.insert(entity);
        this.notifyService.publish(NotifyData.<Void>builder().id(entity.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT_NAT).build());
        return ResultUtil.success(this.initNat(entity));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<Void> deleteComponentNat(int natId) {
        NatEntity entity = this.natMapper.selectById(natId);
        if (entity == null) {
            return ResultUtil.error(ErrorCode.NETWORK_COMPONENT_NAT_NOT_FOUND, "Nat配置不存在");
        }
        this.natMapper.deleteById(natId);
        this.notifyService.publish(NotifyData.<Void>builder().id(entity.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT_NAT).build());
        return ResultUtil.success();
    }
}
