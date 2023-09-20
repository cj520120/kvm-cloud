package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.NotifyMessage;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.model.GuestNetworkModel;
import cn.chenjun.cloud.management.model.NetworkModel;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.CreateNetworkOperate;
import cn.chenjun.cloud.management.operate.bean.DestroyNetworkOperate;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.IpCaculate;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
/**
 * @author chenjun
 */
@Service
public class NetworkService extends AbstractService {

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    public ResultUtil<List<GuestNetworkModel>> listGuestNetworks(int guestId) {
        List<GuestNetworkEntity> networkList = guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", guestId));
        networkList.sort(Comparator.comparingInt(GuestNetworkEntity::getDeviceId));
        List<GuestNetworkModel> models = networkList.stream().map(this::initGuestNetwork).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    public ResultUtil<NetworkModel> getNetworkInfo(int networkId) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        return ResultUtil.success(this.initGuestNetwork(network));
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    public ResultUtil<List<NetworkModel>> listNetwork() {
        List<NetworkEntity> networkList = this.networkMapper.selectList(new QueryWrapper<>());
        List<NetworkModel> models = networkList.stream().map(this::initGuestNetwork).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> createNetwork(String name, String startIp, String endIp, String gateway, String mask, String subnet, String broadcast, String bridge, String dns, int type, int vlanId, int basicNetworkId) {

        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入网络名称");
        }
        if (StringUtils.isEmpty(startIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入开始IP");
        }
        if (StringUtils.isEmpty(endIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入结束IP");
        }
        if (StringUtils.isEmpty(gateway)) {
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
                .type(type)
                .vlanId(vlanId)
                .basicNetworkId(basicNetworkId)
                .secret(UUID.randomUUID().toString().replace("-", ""))
                .status(Constant.NetworkStatus.CREATING).build();
        networkMapper.insert(network);
        List<String> ips = IpCaculate.parseIpRange(startIp, endIp);
        for (String ip : ips) {
            GuestNetworkEntity guestNetwork = GuestNetworkEntity.builder()
                    .guestId(0)
                    .ip(ip)
                    .networkId(network.getNetworkId())
                    .mac(IpCaculate.getRandomMacAddress())
                    .driveType("")
                    .deviceId(0)
                    .build();
            this.guestNetworkMapper.insert(guestNetwork);
        }

        BaseOperateParam operateParam = CreateNetworkOperate.builder().taskId(UUID.randomUUID().toString()).title("创建网络[" + network.getName() + "]").networkId(network.getNetworkId()).build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyMessage.builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return ResultUtil.success(this.initGuestNetwork(network));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
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
        this.notifyService.publish(NotifyMessage.builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return ResultUtil.success(this.initGuestNetwork(network));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> maintenanceNetwork(int networkId) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        network.setStatus(Constant.NetworkStatus.MAINTENANCE);
        this.networkMapper.updateById(network);
        this.notifyService.publish(NotifyMessage.builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return ResultUtil.success(this.initGuestNetwork(network));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> destroyNetwork(int networkId) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }

        if (guestNetworkMapper.selectCount(new QueryWrapper<GuestNetworkEntity>().eq("network_id", networkId).ne("guest_id", 0)) > 0) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "当前网络被其他虚拟机引用，请首先删除虚拟机");
        }
        network.setStatus(Constant.NetworkStatus.DESTROY);
        networkMapper.updateById(network);
        this.guestNetworkMapper.delete(new QueryWrapper<GuestNetworkEntity>().eq("network_id", networkId));
        BaseOperateParam operateParam = DestroyNetworkOperate.builder().taskId(UUID.randomUUID().toString()).title("销毁网络[" + network.getName() + "]").networkId(networkId).build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyMessage.builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
        return ResultUtil.success(this.initGuestNetwork(network));
    }
}
