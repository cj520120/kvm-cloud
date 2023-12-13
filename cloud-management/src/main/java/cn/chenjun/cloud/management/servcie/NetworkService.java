package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.data.mapper.DnsMapper;
import cn.chenjun.cloud.management.data.mapper.NatMapper;
import cn.chenjun.cloud.management.model.ComponentModel;
import cn.chenjun.cloud.management.model.GuestNetworkModel;
import cn.chenjun.cloud.management.model.NatModel;
import cn.chenjun.cloud.management.model.NetworkModel;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.CreateNetworkOperate;
import cn.chenjun.cloud.management.operate.bean.DestroyNetworkOperate;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.IpCalculate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        List<GuestNetworkEntity> networkList = guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.ALLOCATE_ID, guestId).eq(GuestNetworkEntity.ALLOCATE_TYPE, Constant.NetworkAllocateType.GUEST));
        networkList.sort(Comparator.comparingInt(GuestNetworkEntity::getDeviceId));
        List<GuestNetworkModel> models = networkList.stream().map(this::initGuestNetwork).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    public ResultUtil<NetworkModel> getNetworkInfo(int networkId) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            return ResultUtil.error(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        return ResultUtil.success(this.initGuestNetwork(network));
    }

    public ResultUtil<List<NetworkModel>> listNetwork() {
        List<NetworkEntity> networkList = this.networkMapper.selectList(new QueryWrapper<>());
        List<NetworkModel> models = networkList.stream().map(this::initGuestNetwork).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> createNetwork(String name, String startIp, String endIp, String gateway, String mask, String subnet, String broadcast, String bridge, String dns, String domain, int type, int vlanId, int basicNetworkId) {

        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入网络名称");
        }
        if (StringUtils.isEmpty(startIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入开始IP");
        }
        if (StringUtils.isEmpty(endIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入结束IP");
        }
        if (Constant.NetworkType.BASIC == type && StringUtils.isEmpty(gateway)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入网关地址");
        }
        if (StringUtils.isEmpty(mask)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入子网掩码");
        }
        if (StringUtils.isEmpty(bridge)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入桥接网卡名称");
        }
        if (StringUtils.isEmpty(dns)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入DNS信息");
        }
        if (StringUtils.isEmpty(subnet)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入子网信息");
        }
        if (StringUtils.isEmpty(broadcast)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入广播地址");
        }
        if (StringUtils.isEmpty(domain)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入搜索域");
        }
        if (Objects.equals(Constant.NetworkType.VLAN, type) && vlanId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入Vlan ID");
        }
        NetworkEntity network = NetworkEntity.builder()
                .name(name)
                .startIp(startIp)
                .endIp(endIp)
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
                .status(Constant.NetworkStatus.CREATING)
                .createTime(new Date()).build();
        networkMapper.insert(network);
        List<String> ips = IpCalculate.parseIpRange(startIp, endIp);
        for (String ip : ips) {
            GuestNetworkEntity guestNetwork = GuestNetworkEntity.builder()
                    .allocateId(0)
                    .allocateType(Constant.NetworkAllocateType.GUEST)
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
            if (type == Constant.NetworkType.VLAN) {
                basicComponentVip = this.allocateService.allocateNetwork(basicNetworkId);
            }
            ComponentEntity component = ComponentEntity.builder().masterGuestId(0).componentSlaveNumber(0).slaveGuestIds("[]")
                    .componentType(Constant.ComponentType.ROUTE).networkId(network.getNetworkId())
                    .componentVip(componentVip.getIp()).basicComponentVip(basicComponentVip.getIp()).build();
            componentMapper.insert(component);

            componentVip.setAllocateId(component.getComponentId());
            componentVip.setAllocateType(Constant.NetworkAllocateType.COMPONENT_VIP);
            this.guestNetworkMapper.updateById(componentVip);
            if (type == Constant.NetworkType.VLAN) {
                basicComponentVip.setAllocateId(component.getComponentId());
                basicComponentVip.setAllocateType(Constant.NetworkAllocateType.COMPONENT_VIP);
                this.guestNetworkMapper.updateById(componentVip);
                network.setGateway(componentVip.getIp());
                this.networkMapper.updateById(network);
            }
        }
        //申请vlan组件
        if (type == Constant.NetworkType.VLAN) {
            GuestNetworkEntity componentVip = this.allocateService.allocateNetwork(network.getNetworkId());
            GuestNetworkEntity basicComponentVip = this.allocateService.allocateNetwork(basicNetworkId);

            ComponentEntity component = ComponentEntity.builder().masterGuestId(0).componentSlaveNumber(0).slaveGuestIds("[]")
                    .componentType(Constant.ComponentType.NAT).networkId(network.getNetworkId())
                    .componentVip(componentVip.getIp()).basicComponentVip(basicComponentVip.getIp()).build();
            componentMapper.insert(component);

            componentVip.setAllocateId(component.getComponentId());
            componentVip.setAllocateType(Constant.NetworkAllocateType.COMPONENT_VIP);
            this.guestNetworkMapper.updateById(componentVip);
            basicComponentVip.setAllocateId(component.getComponentId());
            basicComponentVip.setAllocateType(Constant.NetworkAllocateType.COMPONENT_VIP);
            this.guestNetworkMapper.updateById(componentVip);
        }
        BaseOperateParam operateParam = CreateNetworkOperate.builder().taskId(UUID.randomUUID().toString()).title("创建网络[" + network.getName() + "]").networkId(network.getNetworkId()).build();
        this.operateTask.addTask(operateParam);
        this.eventService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return ResultUtil.success(this.initGuestNetwork(network));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> registerNetwork(int networkId) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        network.setStatus(Constant.NetworkStatus.CREATING);
        this.networkMapper.updateById(network);
        BaseOperateParam operateParam = CreateNetworkOperate.builder().taskId(UUID.randomUUID().toString()).title("注册网络[" + network.getName() + "]").networkId(network.getNetworkId()).build();
        this.operateTask.addTask(operateParam);
        this.eventService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return ResultUtil.success(this.initGuestNetwork(network));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> maintenanceNetwork(int networkId) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        network.setStatus(Constant.NetworkStatus.MAINTENANCE);
        this.networkMapper.updateById(network);
        this.eventService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return ResultUtil.success(this.initGuestNetwork(network));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> destroyNetwork(int networkId) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }

        if (guestNetworkMapper.selectCount(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_ID, networkId).eq(GuestNetworkEntity.ALLOCATE_TYPE, Constant.NetworkAllocateType.GUEST).ne(GuestNetworkEntity.ALLOCATE_ID, 0)) > 0) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "当前网络被其他虚拟机引用，请首先删除虚拟机");
        }
        network.setStatus(Constant.NetworkStatus.DESTROY);
        this.networkMapper.updateById(network);
        this.dnsMapper.delete(new QueryWrapper<DnsEntity>().eq(DnsEntity.NETWORK_ID, networkId));
        this.guestNetworkMapper.delete(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_ID, networkId));
        List<ComponentEntity> componentList = this.componentMapper.selectList(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.NETWORK_ID, networkId));
        for (ComponentEntity componentEntity : componentList) {
            //取消vip地址
            List<GuestNetworkEntity> guestNetworkEntityList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.ALLOCATE_ID, componentEntity.getComponentId()).eq(GuestNetworkEntity.ALLOCATE_TYPE, Constant.NetworkAllocateType.COMPONENT_VIP));
            for (GuestNetworkEntity guestNetwork : guestNetworkEntityList) {
                guestNetwork.setAllocateType(Constant.NetworkAllocateType.GUEST);
                guestNetwork.setAllocateId(0);
                this.guestNetworkMapper.updateById(guestNetwork);
            }
            this.componentMapper.deleteById(componentEntity);
            this.natMapper.delete(new QueryWrapper<NatEntity>().eq(NatEntity.COMPONENT_ID, componentEntity.getComponentId()));
        }
        BaseOperateParam operateParam = DestroyNetworkOperate.builder().taskId(UUID.randomUUID().toString()).title("销毁网络[" + network.getName() + "]").networkId(networkId).build();
        this.operateTask.addTask(operateParam);
        this.eventService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return ResultUtil.success(this.initGuestNetwork(network));
    }

    public ResultUtil<List<ComponentModel>> listNetworkComponent(int networkId) {
        List<ComponentEntity> entityList = this.componentMapper.selectList(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.NETWORK_ID, networkId));
        List<ComponentModel> list = entityList.stream().map(this::initComponent).collect(Collectors.toList());
        return ResultUtil.success(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<ComponentModel> updateComponentSlaveNumber(int componentId, int slaveNumber) {
        ComponentEntity entity = this.componentMapper.selectById(componentId);
        if (entity == null) {
            return ResultUtil.error(ErrorCode.NETWORK_COMPONENT_NOT_FOUND, "网络组件未找到");
        }
        if (slaveNumber < 0) {
            return ResultUtil.error(ErrorCode.NETWORK_COMPONENT_NOT_FOUND, "Slave数量必须大于等于0");
        }
        entity.setComponentSlaveNumber(slaveNumber);
        this.componentMapper.updateById(entity);
        this.eventService.publish(NotifyData.<Void>builder().id(entity.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT).build());
        return ResultUtil.success(this.initComponent(entity));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<ComponentModel> createComponent(int networkId, int type) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        GuestNetworkEntity componentVip = this.allocateService.allocateNetwork(network.getNetworkId());
        GuestNetworkEntity basicComponentVip = componentVip;
        if (Objects.equals(network.getType(), Constant.NetworkType.VLAN)) {
            basicComponentVip = this.allocateService.allocateNetwork(network.getBasicNetworkId());
        }
        ComponentEntity component = ComponentEntity.builder().masterGuestId(0).componentSlaveNumber(0).slaveGuestIds("[]")
                .componentType(type).networkId(network.getNetworkId())
                .componentVip(componentVip.getIp()).basicComponentVip(basicComponentVip.getIp()).build();
        componentMapper.insert(component);

        componentVip.setAllocateId(component.getComponentId());
        componentVip.setAllocateType(Constant.NetworkAllocateType.COMPONENT_VIP);
        this.guestNetworkMapper.updateById(componentVip);
        if (Objects.equals(network.getType(), Constant.NetworkType.VLAN)) {
            basicComponentVip.setAllocateId(component.getComponentId());
            basicComponentVip.setAllocateType(Constant.NetworkAllocateType.COMPONENT_VIP);
            this.guestNetworkMapper.updateById(componentVip);
            network.setGateway(componentVip.getIp());
            this.networkMapper.updateById(network);
        }
        this.eventService.publish(NotifyData.<Void>builder().id(component.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT).build());
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
            return ResultUtil.error(ErrorCode.NETWORK_COMPONENT_NOT_FOUND, "请删除网络组件对应的虚拟机");
        }
        List<GuestNetworkEntity> list = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.ALLOCATE_ID, component.getComponentId()).eq(GuestNetworkEntity.ALLOCATE_TYPE, Constant.NetworkAllocateType.COMPONENT_VIP));
        for (GuestNetworkEntity guestNetwork : list) {
            guestNetwork.setAllocateId(0);
            guestNetwork.setAllocateType(Constant.NetworkAllocateType.GUEST);
            this.guestNetworkMapper.updateById(guestNetwork);
        }
        this.componentMapper.deleteById(component);
        this.natMapper.delete(new QueryWrapper<NatEntity>().eq(NatEntity.COMPONENT_ID, component.getComponentId()));

        this.eventService.publish(NotifyData.<Void>builder().id(component.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT).build());
        return ResultUtil.<Void>builder().build();
    }

    public ResultUtil<List<NatModel>> listComponentNat(int componentId) {
        List<NatEntity> entityList = this.natMapper.selectList(new QueryWrapper<NatEntity>().eq(NatEntity.COMPONENT_ID, componentId));
        List<NatModel> list = entityList.stream().map(this::initNat).collect(Collectors.toList());
        return ResultUtil.success(list);

    }
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NatModel> createComponentNat(int componentId,int localPort,String protocol,String remoteIp,int remotePort){
        ComponentEntity component = this.componentMapper.selectById(componentId);
        if (component == null) {
            return ResultUtil.error(ErrorCode.NETWORK_COMPONENT_NOT_FOUND, "网络组件不存在");
        }
        NatEntity entity=NatEntity.builder().componentId(componentId).localPort(localPort).protocol(protocol).remotePort(remotePort).remoteIp(remoteIp).createTime(new Date()).build();
        this.natMapper.insert(entity);
        this.eventService.publish(NotifyData.<Void>builder().id(entity.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT_NAT).build());
        return ResultUtil.success(this.initNat(entity));
    }
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<Void> deleteComponentNat(int natId){
        NatEntity entity = this.natMapper.selectById(natId);
        if (entity == null) {
            return ResultUtil.error(ErrorCode.NETWORK_COMPONENT_NAT_NOT_FOUND, "Nat配置不存在");
        }
         this.natMapper.deleteById(natId);
         this.eventService.publish(NotifyData.<Void>builder().id(entity.getComponentId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_COMPONENT_NAT).build());
        return ResultUtil.success( );
    }
}
