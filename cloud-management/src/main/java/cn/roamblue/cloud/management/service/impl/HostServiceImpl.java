package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.common.agent.HostModel;
import cn.roamblue.cloud.common.agent.StorageModel;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.CalculationSchemeInfo;
import cn.roamblue.cloud.management.bean.HostInfo;
import cn.roamblue.cloud.management.data.entity.ClusterEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.VmEntity;
import cn.roamblue.cloud.management.data.mapper.ClusterMapper;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.data.mapper.VmMapper;
import cn.roamblue.cloud.management.service.AgentService;
import cn.roamblue.cloud.management.service.CalculationSchemeService;
import cn.roamblue.cloud.management.service.HostService;
import cn.roamblue.cloud.management.util.BeanConverter;
import cn.roamblue.cloud.management.util.HostStatus;
import cn.roamblue.cloud.management.util.VmStatus;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
public class HostServiceImpl extends AbstractService implements HostService {

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
            ResultUtil<StorageModel> resultUtil = this.agentService.addHostStorage(Constant.StorageType.NFS,uri, storageEntity.getStorageHost(), storageEntity.getStorageSource(), storageEntity.getStorageTarget());
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
        log.error("create host success,host={}", name, uri, hostInfo);
        return hostInfo;
    }

    @Override
    public HostInfo updateHostStatusById(int id, String status) {
        HostEntity entity = hostMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.HOST_NOT_FOUND, "主机不存在");
        } else if (entity.getHostStatus().equals(status)) {
            return this.init(entity);
        }
        entity.setHostStatus(status);
        this.hostMapper.updateById(entity);
        if (HostStatus.MAINTENANCE.equals(status)) {
            List<VmEntity> vmList = this.vmMapper.findByHostId(id);
            vmList.parallelStream().forEach(vmEntity -> {
                this.agentService.stopVm(entity.getHostUri(), vmEntity.getVmName());
                vmEntity.setVmStatus(VmStatus.STOPPED);
                vmEntity.setHostId(0);
                vmEntity.setLastUpdateTime(new Date());
                vmMapper.updateById(vmEntity);
            });

        }
        entity.setHostAllocationCpu(0);
        entity.setHostAllocationMemory(0L);
        this.hostMapper.updateById(entity);
        return this.init(entity);
    }

    @Override
    public void destroyHostById(int id) {

        HostEntity entity = hostMapper.selectById(id);
        if (entity == null) {
            return;
        }
        List<VmEntity> vmList = vmMapper.findByHostId(id).stream().filter(t -> t.getVmStatus().equals(VmStatus.RUNNING)).collect(Collectors.toList());
        if (!vmList.isEmpty()) {
            vmList.parallelStream().forEach(vm -> {
                this.agentService.stopVm(entity.getHostUri(), vm.getVmName());
                vm.setVmStatus(VmStatus.STOPPED);
                vm.setHostId(0);
                vm.setLastUpdateTime(new Date());
                vmMapper.updateById(vm);
            });
        }
        hostMapper.deleteById(id);
        log.info("destroy host success,id={} name={} uri={} uri={}", entity.getId(), entity.getHostName(), entity.getHostUri());

    }


    private HostInfo init(HostEntity entity) {
        HostInfo info = HostInfo.builder()
                .id(entity.getId())
                .name(entity.getHostName())
                .uri(entity.getHostUri())
                .memory(entity.getHostMemory())
                .allocationMemory(entity.getHostAllocationMemory())
                .cpu(entity.getHostCpu())
                .status(entity.getHostStatus())
                .allocationCpu(entity.getHostAllocationCpu())
                .clusterId(entity.getClusterId())
                .createTime(entity.getCreateTime())
                .ip(entity.getHostIp())
                .build();
        return info;
    }
}
