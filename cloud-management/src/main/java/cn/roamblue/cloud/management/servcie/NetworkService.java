package cn.roamblue.cloud.management.servcie;

import cn.hutool.core.convert.impl.BeanConverter;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.GuestNetworkEntity;
import cn.roamblue.cloud.management.data.entity.NetworkEntity;
import cn.roamblue.cloud.management.data.mapper.GuestNetworkMapper;
import cn.roamblue.cloud.management.data.mapper.NetworkMapper;
import cn.roamblue.cloud.management.model.NetworkModel;
import cn.roamblue.cloud.management.operate.bean.BaseOperateParam;
import cn.roamblue.cloud.management.operate.bean.CreateNetworkOperate;
import cn.roamblue.cloud.management.operate.bean.DestroyNetworkOperate;
import cn.roamblue.cloud.management.task.OperateTask;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.IpCaculate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NetworkService {
    @Autowired
    private NetworkMapper networkMapper;

    @Autowired
    private GuestNetworkMapper guestNetworkMapper;
    @Autowired
    private OperateTask operateTask;

    private NetworkModel initNetwork(NetworkEntity entity) {
        return new BeanConverter<>(NetworkModel.class).convert(entity, null);
    }
    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY,write = false)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> getNetworkInfo(int networkId) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        return ResultUtil.success(this.initNetwork(network));
    }
    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY,write = false)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<NetworkModel>> listNetwork() {
        List<NetworkEntity> networkList = this.networkMapper.selectList(new QueryWrapper<>());
        List<NetworkModel> models = networkList.stream().map(this::initNetwork).collect(Collectors.toList());
        return ResultUtil.success(models);
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> createNetwork( String name, String startIp, String endIp, String gateway, String mask, String bridge, String dns, int type, int vlanId, int basicNetworkId) {
        NetworkEntity network = NetworkEntity.builder()
                .name(name)
                .startIp(startIp)
                .endIp(endIp)
                .gateway(gateway)
                .mask(mask)
                .bridge(bridge)
                .dns(dns)
                .type(type)
                .vlanId(vlanId)
                .basicNetworkId(basicNetworkId)
                .status(Constant.NetworkStatus.CREATING).build();
        networkMapper.insert(network);
        List<String> ips = IpCaculate.parseIpRange(startIp, endIp);
        for (String ip : ips) {
            GuestNetworkEntity guestNetwork = GuestNetworkEntity.builder()
                    .guestId(0)
                    .ip(ip)
                    .networkId(network.getNetworkId())
                    .mac(IpCaculate.getMacAddrWithFormat(":"))
                    .driveType("")
                    .deviceId(0)
                    .build();
            this.guestNetworkMapper.insert(guestNetwork);
        }

        BaseOperateParam operateParam = CreateNetworkOperate.builder().taskId(UUID.randomUUID().toString()).title("创建网络[" + network.getName() + "]").networkId(network.getNetworkId()).build();
        this.operateTask.addTask(operateParam);
        return ResultUtil.success(this.initNetwork(network));
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> registerNetwork(int networkId) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        switch (network.getStatus()) {
            case Constant.NetworkStatus.CREATING:
            case Constant.NetworkStatus.READY:
            case Constant.NetworkStatus.MAINTENANCE:
                network.setStatus(Constant.NetworkStatus.CREATING);
                this.networkMapper.updateById(network);
                BaseOperateParam operateParam = CreateNetworkOperate.builder().taskId(UUID.randomUUID().toString()).title("注册网络[" + network.getName() + "]").networkId(network.getNetworkId()).build();
                this.operateTask.addTask(operateParam);
                return ResultUtil.success(this.initNetwork(network));
            default:
                throw new CodeException(ErrorCode.NETWORK_NOT_READY, "网络已销毁");
        }
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> maintenanceNetwork(int networkId) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        switch (network.getStatus()) {
            case Constant.NetworkStatus.READY:
            case Constant.NetworkStatus.MAINTENANCE:
                network.setStatus(Constant.NetworkStatus.MAINTENANCE);
                this.networkMapper.updateById(network);
                return ResultUtil.success(this.initNetwork(network));
            default:
                throw new CodeException(ErrorCode.NETWORK_NOT_READY, "网络未就绪");
        }
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<NetworkModel> destroyNetwork(int networkId) {
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (network == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        switch (network.getStatus()) {
            case Constant.NetworkStatus.READY:
            case Constant.NetworkStatus.ERROR:
                if(guestNetworkMapper.selectCount(new QueryWrapper<GuestNetworkEntity>().eq("network_id",networkId).ne("guest_id",0))>0){
                    throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "当前网络被其他虚拟机引用，请首先删除虚拟机");
                }
                network.setStatus(Constant.NetworkStatus.DESTROY);
                networkMapper.updateById(network);
                this.guestNetworkMapper.delete(new QueryWrapper<GuestNetworkEntity>().eq("network_id",networkId));
                BaseOperateParam operateParam = DestroyNetworkOperate.builder().taskId(UUID.randomUUID().toString()).title("销毁网络[" + network.getName() + "]").networkId(networkId).build();
                this.operateTask.addTask(operateParam);
                return ResultUtil.success(this.initNetwork(network));
            default:
                throw new CodeException(ErrorCode.NETWORK_NOT_READY, "网络未就绪");
        }
    }
}
