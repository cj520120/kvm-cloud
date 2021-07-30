package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.CalculationSchemeInfo;
import cn.roamblue.cloud.management.data.entity.ClusterEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.VmEntity;
import cn.roamblue.cloud.management.data.mapper.ClusterMapper;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.data.mapper.VmMapper;
import cn.roamblue.cloud.management.service.AllocateService;
import cn.roamblue.cloud.management.service.CalculationSchemeService;
import cn.roamblue.cloud.management.util.HostStatus;
import cn.roamblue.cloud.management.util.StorageStatus;
import cn.roamblue.cloud.management.util.VmStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
@Slf4j
public class AllocateServiceImpl implements AllocateService {
    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private ClusterMapper clusterMapper;
    @Autowired
    private VmMapper vmMapper;
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
                if (hostEntity.getHostAllocationCpu() != totalCpu || hostEntity.getHostAllocationMemory() != totalMemory) {
                    hostEntity.setHostAllocationCpu(totalCpu);
                    hostEntity.setHostAllocationMemory(totalMemory);
                    hostMapper.updateById(hostEntity);
                }
            }

        }
    }

    @Override
    public StorageEntity allocateStorage(int clusterId, int storageId, long size) {
        StorageEntity storage;
        if (storageId > 0) {
            storage = storageMapper.selectById(storageId);
            if (storage == null) {
                throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
            }
            if (!storage.getStorageStatus().equalsIgnoreCase(StorageStatus.READY)) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
        } else {
            List<StorageEntity> list = storageMapper.findByClusterId(clusterId);
            if (list.isEmpty()) {
                throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "没有配置存储池");

            }
            list = list.stream().filter(t -> t.getStorageStatus().equals(StorageStatus.READY)).collect(Collectors.toList());
            if (list.isEmpty()) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "没有可用存储池");
            }
            storage = list.stream().filter(t -> t.getStorageAllocation() + size < t.getStorageCapacity()).findAny().orElseThrow(() -> new CodeException(ErrorCode.STORAGE_NOT_SPACE, "存储池没有可用空间"));
        }
        return storage;
    }

    @Override
    public HostEntity allocateHost(int clusterId, int hostId, int cpu, long memory) {

        log.info("开始申请主机:clusterId={}, int hostId={},  cpu={},  memory={}", clusterId, hostId, cpu, memory);
        if (hostId > 0) {
            HostEntity entity = this.hostMapper.selectById(hostId);
            if (entity == null) {
                throw new CodeException(ErrorCode.HOST_NOT_FOUND, "申请主机失败，主机未就绪或资源不足");
            }
            this.refreshHost(entity);
            if ((entity.getHostAllocationCpu() + cpu > entity.getHostCpu()) || (entity.getHostAllocationMemory() + memory > entity.getHostMemory())) {
                throw new CodeException(ErrorCode.HOST_NOT_SPACE, "主机未就绪或资源不足");
            }
            return entity;
        } else {
            List<HostEntity> list = this.hostMapper.findByClusterId(clusterId);
            list.forEach(this::refreshHost);
            list = list.stream().filter(t -> (t.getHostAllocationCpu() + cpu < t.getHostCpu()) && (t.getHostAllocationMemory() + memory < t.getHostMemory()) && t.getHostStatus().equals(HostStatus.READY))
                    .collect(Collectors.toList());
            list.sort((o1, o2) -> {
                long memory1 = o1.getHostMemory() - o1.getHostAllocationMemory();
                long memory2 = o2.getHostMemory() - o2.getHostAllocationMemory();
                int result = Long.compare(memory1, memory2);
                if (result == 0) {
                    int cpu1 = o1.getHostCpu() - o1.getHostAllocationCpu();
                    int cpu2 = o2.getHostCpu() - o2.getHostAllocationCpu();
                    result = Integer.compare(cpu1, cpu2);
                }
                return result;
            });
            if (list.isEmpty()) {
                throw new CodeException(ErrorCode.HOST_NOT_SPACE, "主机未就绪或资源不足");
            }
            return list.get(list.size() - 1);
        }
    }

}
