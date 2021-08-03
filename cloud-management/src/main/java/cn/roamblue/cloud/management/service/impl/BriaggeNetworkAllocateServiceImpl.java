package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.VmNetworkInfo;
import cn.roamblue.cloud.management.data.entity.VmNetworkEntity;
import cn.roamblue.cloud.management.data.mapper.VmNetworkMapper;
import cn.roamblue.cloud.management.service.NetworkAllocateService;
import cn.roamblue.cloud.management.util.IpType;
import cn.roamblue.cloud.management.util.NetworkType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class BriaggeNetworkAllocateServiceImpl extends AbstractService implements NetworkAllocateService {

    @Autowired
    private VmNetworkMapper vmNetworkMapper;

    @Override
    public VmNetworkInfo allocateManagerAddress(int networkId, int vmId) {
        return this.allocate(networkId, vmId, IpType.MANAGER);
    }

    @Override
    public VmNetworkInfo allocateGuestAddress(int networkId, int vmId) {
        return this.allocate(networkId, vmId, IpType.GUEST);
    }

    private VmNetworkInfo allocate(int networkId, int vmId, String ipType) {
        int deviceId = 0;
        List<Integer> deviceIds = vmNetworkMapper.findByVmId(vmId).stream().map(VmNetworkEntity::getVmDevice).collect(Collectors.toList());

        while (deviceIds.contains(deviceId)) {
            deviceId++;
        }
        VmNetworkEntity instanceNetworkEntity = null;
        boolean allocate = false;

        List<VmNetworkEntity> list = vmNetworkMapper.findEmptyNetworkByNetworkIdAndIpType(networkId, ipType);
        while (list.size() > 0 && !allocate) {
            instanceNetworkEntity = list.remove(0);
            allocate = vmNetworkMapper.allocateNetwork(instanceNetworkEntity.getId(), vmId, deviceId) > 0;
        }
        if (!allocate) {
            throw new CodeException(ErrorCode.NETWORK_NOT_SPACE, localeMessage.getMessage("ALLOCATE_ADDRESS_NOT_RESOURCE", "网络不可用或无可用地址"));
        }
        instanceNetworkEntity.setVmDevice(deviceId);
        instanceNetworkEntity.setVmId(vmId);
        VmNetworkInfo info = this.initInstanceNetwork(instanceNetworkEntity);
        log.info("allocate network success.networkIds={} vmId={}", networkId, vmId);
        return info;
    }

    @Override
    public String getType() {
        return NetworkType.BRIDGE;
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
                .status(entity.getNetworkStatus())
                .createTime(entity.getCreateTime())
                .build();
    }

}
