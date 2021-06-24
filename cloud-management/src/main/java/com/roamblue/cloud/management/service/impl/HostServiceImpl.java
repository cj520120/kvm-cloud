package com.roamblue.cloud.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.roamblue.cloud.common.agent.HostModel;
import com.roamblue.cloud.common.agent.StorageModel;
import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.common.error.CodeException;
import com.roamblue.cloud.common.util.ErrorCode;
import com.roamblue.cloud.management.bean.CalculationSchemeInfo;
import com.roamblue.cloud.management.bean.HostInfo;
import com.roamblue.cloud.management.data.entity.ClusterEntity;
import com.roamblue.cloud.management.data.entity.HostEntity;
import com.roamblue.cloud.management.data.entity.StorageEntity;
import com.roamblue.cloud.management.data.entity.VmEntity;
import com.roamblue.cloud.management.data.mapper.ClusterMapper;
import com.roamblue.cloud.management.data.mapper.HostMapper;
import com.roamblue.cloud.management.data.mapper.StorageMapper;
import com.roamblue.cloud.management.data.mapper.VmMapper;
import com.roamblue.cloud.management.service.AgentService;
import com.roamblue.cloud.management.service.CalculationSchemeService;
import com.roamblue.cloud.management.service.HostService;
import com.roamblue.cloud.management.util.BeanConverter;
import com.roamblue.cloud.management.util.HostStatus;
import com.roamblue.cloud.management.util.VmStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class HostServiceImpl implements HostService {

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private StorageMapper storageMapper;

    @Autowired
    private AgentService agentService;

    @Autowired
    private VmMapper vmMapper;
    @Autowired
    private ClusterMapper clusterMapper;

    @Autowired
    private CalculationSchemeService calculationSchemeService;

    private void refreshHost(HostEntity hostEntity) {
        ClusterEntity clusterEntity = clusterMapper.selectById(hostEntity.getClusterId());

        if (clusterEntity != null) {
            float overCpu = clusterEntity.getOverCpu();
            float overMemory = clusterEntity.getOverMemory();
            hostEntity.setHostCpu((int) (overCpu * hostEntity.getHostCpu()));
            hostEntity.setHostMemory((long) (overMemory * hostEntity.getHostMemory()));
        }
        List<CalculationSchemeInfo> calculationSchemeInfoList = calculationSchemeService.listCalculationScheme();
        if (calculationSchemeInfoList != null && !calculationSchemeInfoList.isEmpty()) {
            Map<Integer, CalculationSchemeInfo> map = calculationSchemeInfoList.stream().collect(Collectors.toMap(CalculationSchemeInfo::getId, Function.identity()));
            List<VmEntity> instanceList = vmMapper.findByHostId(hostEntity.getId()).stream().filter(t -> t.getVmStatus().equals(VmStatus.RUNNING)).collect(Collectors.toList());
            int totalCpu = 0;
            long totalMemory = 0L;
            for (VmEntity instanceEntity : instanceList) {
                CalculationSchemeInfo calculationSchemeInfo = map.get(instanceEntity.getCalculationSchemeId());
                if (calculationSchemeInfo != null) {
                    totalCpu += calculationSchemeInfo.getCpu();
                    totalMemory += calculationSchemeInfo.getMemory();
                }
            }
            if (hostEntity.getHostAllocationCpu() != totalCpu || hostEntity.getHostAllocationMemory() != totalMemory) {
                HostEntity updateAllocation = HostEntity.builder()
                        .id(hostEntity.getId())
                        .hostAllocationCpu(totalCpu)
                        .hostAllocationMemory(totalMemory).build();
                hostMapper.updateById(updateAllocation);
            }

        }
    }

    @Override
    public List<HostInfo> listHost() {

        List<HostEntity> hostEntityList = hostMapper.selectAll();
        hostEntityList.forEach(this::refreshHost);
        List<HostInfo> list = BeanConverter.convert(hostEntityList, this::init);
        return list;
    }

    @Override
    public List<HostInfo> search(int clusterId) {

        QueryWrapper<HostEntity> wrapper = new QueryWrapper<>();
        if (clusterId > 0) {
            wrapper.eq("cluster_id", clusterId);
        }
        List<HostEntity> hostEntityList = hostMapper.selectList(wrapper);

        hostEntityList.forEach(this::refreshHost);
        List<HostInfo> list = BeanConverter.convert(hostEntityList, this::init);
        return list;
    }


    @Override
    public HostInfo findHostById(int id) {

        HostEntity entity = hostMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.HOST_NOT_FOUND, "主机不存在");
        }
        refreshHost(entity);
        HostInfo info = init(entity);
        return info;
    }

    @Override
    public HostInfo createHost(int clusterId, String name, String ip, String uri) {

        QueryWrapper<HostEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("host_ip", ip);
        if (hostMapper.selectCount(queryWrapper) > 0) {
            throw new CodeException(ErrorCode.HOST_EXISTS, "创建主机失败，主机信息已经存在");
        }
        ResultUtil<HostModel> hostInfoResultUtil = this.agentService.getHostInfo(uri);
        if (hostInfoResultUtil.getCode() != ErrorCode.SUCCESS) {
            throw new CodeException(hostInfoResultUtil.getCode(), hostInfoResultUtil.getMessage());
        }
        HostModel kvmHostInfo = hostInfoResultUtil.getData();
        List<StorageEntity> storageList = this.storageMapper.findByClusterId(clusterId);
        for (StorageEntity storageEntity : storageList) {
            ResultUtil<StorageModel> resultUtil = this.agentService.addHostStorage(uri, storageEntity.getStorageHost(), storageEntity.getStorageSource(), storageEntity.getStorageTarget());
            if (resultUtil.getCode() != ErrorCode.SUCCESS) {
                throw new CodeException(resultUtil.getCode(), resultUtil.getMessage());
            }
        }
        HostEntity entity = HostEntity.builder()
                .hostCpu(kvmHostInfo.getCpu())
                .hostName(name)
                .hostIp(ip)
                .hostUri(uri)
                .hostMemory(kvmHostInfo.getMemory())
                .hostAllocationCpu(0)
                .hostAllocationMemory(0L)
                .clusterId(clusterId)
                .hostStatus(HostStatus.READY)
                .createTime(new Date())
                .build();
        hostMapper.insert(entity);
        HostInfo hostInfo = init(entity);
        log.error("创建主机成功,host={}", name, uri, hostInfo);
        return hostInfo;
    }

    @Override
    public void destroyHostById(int id) {

        HostEntity entity = hostMapper.selectById(id);
        if (entity == null) {
            return;
        }
        List<VmEntity> vmList = vmMapper.findByHostId(id).stream().filter(t -> t.getVmStatus().equals(VmStatus.RUNNING)).collect(Collectors.toList());
        if (!vmList.isEmpty()) {
            for (VmEntity vm : vmList) {
                this.agentService.destroyVm(entity.getHostUri(), vm.getVmName());
                vm.setVmStatus(VmStatus.STOPPED);
                vmMapper.updateById(vm);
            }
        }
        hostMapper.deleteById(id);
        log.info("删除主机成功,id={} name={} uri={} uri={}", entity.getId(), entity.getHostName(), entity.getHostUri());

    }


    private HostInfo init(HostEntity entity) {
        HostInfo info = HostInfo.builder()
                .id(entity.getId())
                .name(entity.getHostName())
                .uri(entity.getHostUri())
                .memory(entity.getHostMemory())
                .allocationMemory(entity.getHostAllocationMemory())
                .cpu(entity.getHostCpu())
                .status(HostStatus.READY)
                .allocationCpu(entity.getHostAllocationCpu())
                .clusterId(entity.getClusterId())
                .createTime(entity.getCreateTime())
                .ip(entity.getHostIp())
                .build();
        return info;
    }
}
