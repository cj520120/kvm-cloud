package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class AllocateService extends AbstractService {


    public StorageEntity allocateStorage(int category, int storageId) {
        StorageEntity storage;
        if (storageId > 0) {
            storage = storageMapper.selectById(storageId);
            if (storage == null) {
                throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
            }
            boolean isSupport = (storage.getSupportCategory().intValue() & category) == category;
            if (!isSupport) {
                throw new CodeException(ErrorCode.STORAGE_NOT_SUPPORT, "选择的存储磁盘分类不支持选定类型");
            }
        } else {
            List<StorageEntity> storageList = storageMapper.selectList(new QueryWrapper<>());
            storageList = storageList.stream().filter(t -> {
                boolean isSupport = !Objects.equals(t.getType(), cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL);//本地磁盘不参与自动分配
                isSupport = isSupport && (t.getSupportCategory().intValue() & category) == category;
                return isSupport && Objects.equals(t.getStatus(), Constant.StorageStatus.READY);
            }).collect(Collectors.toList());
            Map<Integer, Float> scoreMap = new HashMap<>();
            for (StorageEntity entity : storageList) {
                List<ConfigQuery> queryList = Arrays.asList(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).id(0).build(), ConfigQuery.builder().type(Constant.ConfigType.STORAGE).id(entity.getStorageId()).build());
                float storageWeight = this.configService.getConfig(queryList, ConfigKey.DEFAULT_ALLOCATE_STORAGE_WEIGHT);
                float availableValue = entity.getAvailable() / (1024.0f * 1024.0f * 1024.0f * 1024.0f);
                float score = availableValue * storageWeight;
                scoreMap.put(entity.getStorageId(), score);
                log.info("分配存储池计算权重,storage={},weight={},available={}TB,score={}", entity.getDescription(), storageWeight, availableValue, score);
            }
            log.info("开始重新计算存储池排序");
            storageList.sort((o1, o2) -> -Float.compare(scoreMap.get(o1.getStorageId()), scoreMap.get(o2.getStorageId())));
            log.info("结束存储池排序");
            storage = storageList.stream().findFirst().orElseThrow(() -> new CodeException(ErrorCode.STORAGE_NOT_SPACE, "没有可用的存储池资源"));
        }
        return storage;
    }

    public GuestNetworkEntity allocateNetwork(int networkId) {
        QueryWrapper<GuestNetworkEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("network_id", networkId);
        wrapper.eq("allocate_id", 0);
        wrapper.eq("allocate_type", Constant.NetworkAllocateType.GUEST);
        wrapper.last("limit 0,1");
        GuestNetworkEntity guestNetwork = guestNetworkMapper.selectOne(wrapper);
        if (guestNetwork == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_SPACE, "没有可用的网络资源");
        }
        return guestNetwork;
    }

    public List<HostEntity> listAllocateHost(int cpu, long memory) {
        List<HostEntity> list = this.hostMapper.selectList(new QueryWrapper<>());
        for (HostEntity host : list) {
            List<ConfigQuery> queryList = Arrays.asList(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).build(), ConfigQuery.builder().type(Constant.ConfigType.HOST).id(host.getHostId()).build());
            host.setTotalCpu((int) (host.getTotalCpu() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_CPU)));
            host.setTotalMemory((long) (host.getTotalMemory() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_MEMORY)));
        }
        list = list.stream().filter(t -> hostVerify(t, cpu, memory))
                .collect(Collectors.toList());
        return list;
    }

    public HostEntity allocateHost(int hostId, int mustHostId, int cpu, long memory) {
        if (mustHostId > 0) {
            HostEntity host = this.hostMapper.selectById(mustHostId);
            List<ConfigQuery> queryList = Arrays.asList(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).build(), ConfigQuery.builder().type(Constant.ConfigType.HOST).id(host.getHostId()).build());
            host.setTotalCpu((int) (host.getTotalCpu() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_CPU)));
            host.setTotalMemory((long) (host.getTotalMemory() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_MEMORY)));
            if (!hostVerify(host, cpu, memory)) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "主机没有可用资源");
            }
            return host;
        } else {
            List<HostEntity> list = this.hostMapper.selectList(new QueryWrapper<>());
            for (HostEntity host : list) {

                List<ConfigQuery> queryList = Arrays.asList(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).build(), ConfigQuery.builder().type(Constant.ConfigType.HOST).id(host.getHostId()).build());
                host.setTotalCpu((int) (host.getTotalCpu() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_CPU)));
                host.setTotalMemory((long) (host.getTotalMemory() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_MEMORY)));
            }
            //获取满足的主机列表
            list = list.stream().filter(t -> hostVerify(t, cpu, memory)).collect(Collectors.toList());

            Collections.shuffle(list);
            HostEntity host = null;
            if (hostId > 0) {
                host = list.stream().filter(t -> Objects.equals(t.getHostId(), hostId)).findFirst().orElse(null);
            }
            if (host == null) {
                if (cpu > 0 && memory > 0) {
                    Map<Integer, Float> scoreMap = new HashMap<>();
                    for (HostEntity entity : list) {
                        List<ConfigQuery> queryList = Arrays.asList(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).id(0).build(), ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).id(entity.getHostId()).build());
                        float cpuWeight = this.configService.getConfig(queryList, ConfigKey.DEFAULT_ALLOCATE_HOST_CPU_WEIGHT);
                        float memoryWeight = this.configService.getConfig(queryList, ConfigKey.DEFAULT_ALLOCATE_HOST_MEMORY_WEIGHT);
                        float overCpu = this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_CPU);
                        float overMemory = this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_MEMORY);
                        float availableCpu = entity.getTotalCpu() * overCpu - entity.getAllocationCpu();
                        float availableMemory = (entity.getTotalMemory() * overMemory - entity.getAllocationMemory()) / (1024 * 1024);

                        float cpuScore = availableCpu * cpuWeight;
                        float memoryScore = availableMemory * memoryWeight;
                        float score = cpuScore + memoryScore;
                        scoreMap.put(entity.getHostId(), score);
                        log.info("分配主机计算权重,host={},cpu-weight={},memory-weight={},available-cpu={},available-memory={}GB,score={}", entity.getDisplayName(), cpuWeight, memoryWeight, availableCpu, availableMemory, score);
                    }
                    list.sort((o1, o2) -> -Float.compare(scoreMap.get(o1.getHostId()), scoreMap.get(o2.getHostId())));
                }
                host = list.stream().findFirst().orElseThrow(() -> new CodeException(ErrorCode.HOST_NOT_SPACE, "没有可用的主机资源"));
            }
            return host;
        }
    }

    private boolean hostVerify(HostEntity host, int cpu, long memory) {
        if (!Objects.equals(host.getStatus(), Constant.HostStatus.ONLINE)) {
            return false;
        }
        int allocateCpu = host.getAllocationCpu() + cpu;
        long allocationMemory = host.getAllocationMemory() + memory;
        return host.getTotalCpu() > allocateCpu && host.getTotalMemory() > allocationMemory;
    }

    @Transactional(rollbackFor = Exception.class)
    public void initHostAllocate() {
        List<HostEntity> hosts = this.hostMapper.selectList(new QueryWrapper<>());
        Map<Integer, List<GuestEntity>> map = guestMapper.selectList(new QueryWrapper<GuestEntity>().gt(GuestEntity.HOST_ID, 0)).stream().collect(Collectors.groupingBy(GuestEntity::getHostId));
        for (HostEntity host : hosts) {
            List<GuestEntity> guestList = map.get(host.getHostId());
            if (guestList == null) {
                host.setAllocationCpu(0);
                host.setAllocationMemory(0L);
            } else {
                host.setAllocationCpu(guestList.stream().mapToInt(GuestEntity::getCpu).sum());
                host.setAllocationMemory(guestList.stream().mapToLong(GuestEntity::getMemory).sum());
            }
            this.hostMapper.updateById(host);
        }
    }
}
