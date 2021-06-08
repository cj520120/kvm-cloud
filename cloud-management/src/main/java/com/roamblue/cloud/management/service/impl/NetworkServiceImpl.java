package com.roamblue.cloud.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.roamblue.cloud.common.error.CodeException;
import com.roamblue.cloud.common.util.ErrorCode;
import com.roamblue.cloud.management.bean.NetworkInfo;
import com.roamblue.cloud.management.bean.VmNetworkInfo;
import com.roamblue.cloud.management.data.entity.ClusterEntity;
import com.roamblue.cloud.management.data.entity.NetworkEntity;
import com.roamblue.cloud.management.data.entity.VmNetworkEntity;
import com.roamblue.cloud.management.data.mapper.ClusterMapper;
import com.roamblue.cloud.management.data.mapper.NetworkMapper;
import com.roamblue.cloud.management.data.mapper.VmNetworkMapper;
import com.roamblue.cloud.management.service.NetworkService;
import com.roamblue.cloud.management.util.BeanConverter;
import com.roamblue.cloud.management.util.IpCaculate;
import com.roamblue.cloud.management.util.IpType;
import com.roamblue.cloud.management.util.NetworkStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class NetworkServiceImpl implements NetworkService {
    @Autowired
    private ClusterMapper clusterMapper;
    @Autowired
    private NetworkMapper networkMapper;
    @Autowired
    private VmNetworkMapper vmNetworkMapper;

    @Override
    public List<NetworkInfo> listNetwork() {

        List<NetworkEntity> networkEntityList = networkMapper.selectList(new QueryWrapper<>());
        List<NetworkInfo> list = BeanConverter.convert(networkEntityList, this::init);
        return list;
    }

    @Override
    public List<NetworkInfo> search(int clusterId) {

        QueryWrapper<NetworkEntity> wrapper = new QueryWrapper<>();
        if (clusterId > 0) {
            wrapper.eq("cluster_id", clusterId);
        }
        List<NetworkEntity> networkEntityList = networkMapper.selectList(wrapper);
        List<NetworkInfo> list = BeanConverter.convert(networkEntityList, this::init);
        return list;
    }

    @Override
    public List<NetworkInfo> listNetworkByClusterId(int clusterId) {

        List<NetworkEntity> networkEntityList = networkMapper.findByClusterId(clusterId);
        List<NetworkInfo> list = BeanConverter.convert(networkEntityList, this::init);
        return list;
    }

    @Override
    public NetworkInfo findNetworkById(int id) {

        NetworkEntity entity = networkMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        NetworkInfo info = init(entity);
        return info;
    }

    @Override
    public List<VmNetworkInfo> listVmNetworkByNetworkId(int networkId) {

        List<VmNetworkEntity> list = this.vmNetworkMapper.findByNetworkId(networkId);

        List<VmNetworkInfo> result = BeanConverter.convert(list, this::initInstanceNetwork);
        return result;
    }

    @Override
    public NetworkInfo createNetwork(String name, int clusterId, String managerStartIp, String managerEndIp, String guestStartIp, String guestEndIp, String subnet, String gateway, String dns, String card, String type) {

        ClusterEntity clusterEntity = this.clusterMapper.selectById(clusterId);
        if (clusterEntity == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "集群不存在");
        }

        NetworkEntity entity = NetworkEntity.builder().networkName(name)
                .clusterId(clusterId)
                .networkManagerStartIp(managerStartIp)
                .networkManagerEndIp(managerEndIp)
                .networkGuestStartIp(guestStartIp)
                .networkGuestEndIp(guestEndIp)
                .networkSubnet(subnet)
                .networkGateway(gateway)
                .networkDns(dns)
                .networkCard(card)
                .networkType(type)
                .networkStatus(NetworkStatus.READY)
                .createTime(new Date())
                .build();
        networkMapper.insert(entity);
        List<String> ipList = IpCaculate.parseIpRange(guestStartIp, guestEndIp);
        for (String ip : ipList) {
            VmNetworkEntity vmNetworkEntity = VmNetworkEntity.builder()
                    .clusterId(clusterId)
                    .networkId(entity.getId())
                    .vmId(0)
                    .vmDevice(0)
                    .networkIp(ip)
                    .networkMac(IpCaculate.getMacAddrWithFormat(":"))
                    .networkStatus(NetworkStatus.READY)
                    .ipType(IpType.GUEST)
                    .createTime(new Date())
                    .build();
            vmNetworkMapper.insert(vmNetworkEntity);

        }
        ipList = IpCaculate.parseIpRange(managerStartIp, managerEndIp);
        for (String ip : ipList) {
            VmNetworkEntity vmNetworkEntity = VmNetworkEntity.builder()
                    .clusterId(clusterId)
                    .networkId(entity.getId())
                    .vmId(0)
                    .vmDevice(0)
                    .networkIp(ip)
                    .networkMac(IpCaculate.getMacAddrWithFormat(":"))
                    .networkStatus(NetworkStatus.READY)
                    .ipType(IpType.MANAGER)
                    .createTime(new Date())
                    .build();
            vmNetworkMapper.insert(vmNetworkEntity);

        }
        NetworkInfo info = init(entity);
        log.info("创建网络信息成功.network={}", info);
        return info;
    }

    @Override
    public void destroyNetworkById(int id) {
        QueryWrapper<VmNetworkEntity> wrapper = new QueryWrapper<>();
        wrapper.gt("vm_id", 0);
        wrapper.eq("network_id", id);
        if (vmNetworkMapper.selectCount(wrapper) > 0) {
            throw new CodeException(ErrorCode.HAS_VM_ERROR, "网络包含主机信息");
        }
        QueryWrapper<VmNetworkEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("network_id", id);
        vmNetworkMapper.delete(queryWrapper);
        networkMapper.deleteById(id);
        log.info("销毁网络信息成功.network={}", id);

    }

    @Override
    public void detachVmNetworkByVmId(int vmId) {
        vmNetworkMapper.freeByVmId(vmId);
        log.info("回收网络信息成功. vmId={}", vmId);
    }

    @Override
    public NetworkInfo startNetworkById(int id) {
        NetworkEntity entity = networkMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        entity.setNetworkStatus(NetworkStatus.READY);
        networkMapper.updateById(entity);
        return init(entity);
    }

    @Override
    public NetworkInfo stopNetworkById(int id) {
        NetworkEntity entity = networkMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        entity.setNetworkStatus(NetworkStatus.STOP);
        networkMapper.updateById(entity);
        return init(entity);
    }

    @Override
    public List<VmNetworkInfo> findVmNetworkByVmId(int vmId) {

        List<VmNetworkEntity> entityList = vmNetworkMapper.findByVmId(vmId);
        List<VmNetworkInfo> list = BeanConverter.convert(entityList, this::initInstanceNetwork);
        return list;
    }

    private VmNetworkInfo initInstanceNetwork(VmNetworkEntity entity) {
        return VmNetworkInfo.builder()
                .id(entity.getId())
                .networkId(entity.getNetworkId())
                .clusterId(entity.getClusterId())
                .vmId(entity.getVmId())
                .device(entity.getVmDevice())
                .mac(entity.getNetworkMac())
                .ip(entity.getNetworkIp())
                .type(entity.getIpType())
                .status(entity.getNetworkStatus())
                .createTime(entity.getCreateTime())
                .build();
    }

    private NetworkInfo init(NetworkEntity entity) {
        return NetworkInfo.builder().id(entity.getId())
                .name(entity.getNetworkName())
                .clusterId(entity.getClusterId())
                .guestStartIp(entity.getNetworkGuestStartIp())
                .guestEndIp(entity.getNetworkGuestEndIp())
                .managerStartIp(entity.getNetworkManagerStartIp())
                .managerEndIp(entity.getNetworkManagerEndIp())
                .subnet(entity.getNetworkSubnet())
                .gateway(entity.getNetworkGateway())
                .dns(entity.getNetworkDns())
                .card(entity.getNetworkCard())
                .status(entity.getNetworkStatus())
                .type(entity.getNetworkType())
                .createTime(entity.getCreateTime())
                .build();
    }
}
