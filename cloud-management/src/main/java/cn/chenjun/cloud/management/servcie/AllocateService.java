package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.HostRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class AllocateService extends AbstractService {


    public StorageEntity allocateStorage(int category, int storageId) {
        if (storageId > 0) {
            StorageEntity storage = storageDao.findById(storageId);
            if (storage == null) {
                throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
            }
            if ((storage.getSupportCategory() & category) != category) {
                throw new CodeException(ErrorCode.STORAGE_NOT_SUPPORT, "选择的存储磁盘分类不支持选定类型");
            }
            return storage;
        }

        List<StorageEntity> storageList = storageDao.listAll().stream().filter(
                storage -> (storage.getSupportCategory() & category) == category
                        && storage.getStatus() == cn.chenjun.cloud.common.util.Constant.StorageStatus.READY
                        && storage.getType() != cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL
        ).collect(Collectors.toList());
        Map<Integer, Float> scoreMap = storageList.parallelStream().collect(Collectors.toMap(StorageEntity::getStorageId, entity -> {
            List<ConfigQuery> queryList = Arrays.asList(
                    ConfigQuery.builder().type(cn.chenjun.cloud.common.util.Constant.ConfigType.DEFAULT).id(0).build(),
                    ConfigQuery.builder().type(cn.chenjun.cloud.common.util.Constant.ConfigType.STORAGE).id(entity.getStorageId()).build()
            );
            float storageWeight = this.configService.getConfig(queryList, ConfigKey.DEFAULT_ALLOCATE_STORAGE_WEIGHT);
            float availableValue = entity.getAvailable() / (1024.0f * 1024.0f * 1024.0f * 1024.0f);
            return availableValue * storageWeight;
        }));

        return storageList.stream()
                .max((o1, o2) -> Float.compare(scoreMap.get(o1.getStorageId()), scoreMap.get(o2.getStorageId())))
                .orElseThrow(() -> new CodeException(ErrorCode.STORAGE_NOT_SPACE, "没有可用的存储池资源"));
    }

    @Transactional(rollbackFor = Exception.class)
    public GuestNetworkEntity allocateNetwork(int networkId, int allocateId, int allocateType, int deviceId, String device, String allocateDescription) {
        GuestNetworkEntity guestNetwork = guestNetworkDao.allocate(networkId);//.selectOne(wrapper);
        if (guestNetwork == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_SPACE, "没有可用的网络资源，networkId=" + networkId);
        }
        guestNetwork.setAllocateId(allocateId);
        guestNetwork.setAllocateType(allocateType);
        guestNetwork.setDeviceId(deviceId);
        guestNetwork.setDeviceType(device);
        guestNetwork.setAllocateDescription(allocateDescription);
        this.guestNetworkDao.update(guestNetwork);
        return guestNetwork;
    }

    @Transactional(rollbackFor = Exception.class)
    public GuestNetworkEntity releaseNetwork(int guestNetworkId) {
        GuestNetworkEntity guestNetwork = this.guestNetworkDao.findById(guestNetworkId);
        if (guestNetwork == null) {
            throw new CodeException(ErrorCode.NETWORK_NIC_NOT_FOUND, "网络不存在");
        }
        guestNetwork.setAllocateId(0);
        guestNetwork.setAllocateType(cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.DEFAULT);
        guestNetwork.setAllocateDescription("");
        guestNetwork.setDeviceId(0);
        guestNetwork.setDeviceType(Constant.NetworkDriver.VIRTIO);
        this.guestNetworkDao.update(guestNetwork);
        return guestNetwork;
    }

    public HostEntity allocateHost(int role, int hostId, String arch, int mustHostId, int cpu, long memory) {
        if (mustHostId > 0) {
            HostEntity host = this.hostDao.findById(mustHostId);
            if (role!= HostRole.NONE && (host.getRole() & role) != role) {
                throw new CodeException(ErrorCode.HOST_ROLE_NOT_SUPPORT, "主机角色不支持");
            }
            List<ConfigQuery> queryList = Arrays.asList(ConfigQuery.builder().type(cn.chenjun.cloud.common.util.Constant.ConfigType.DEFAULT).build(), ConfigQuery.builder().type(cn.chenjun.cloud.common.util.Constant.ConfigType.HOST).id(host.getHostId()).build());
            host.setTotalCpu((int) (host.getTotalCpu() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_CPU)));
            host.setTotalMemory((long) (host.getTotalMemory() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_MEMORY)));
            if (host.getStatus() != cn.chenjun.cloud.common.util.Constant.HostStatus.ONLINE) {
                throw new CodeException(ErrorCode.HOST_NOT_READY, "主机状态不在线");
            }
            if (!hostVerify(host, cpu, memory, arch)) {
                throw new CodeException(ErrorCode.HOST_NOT_RESOURCE, "主机没有可用资源");
            }
            return host;
        } else {
            List<HostEntity> list = this.hostDao.listAll();
            if (role!= HostRole.NONE) {
                list.removeIf(t -> !HostRole.hasRole(t.getRole(), role));
                log.info("过滤主机角色,role={}", role);
            }
            for (HostEntity host : list) {
                List<ConfigQuery> queryList = Arrays.asList(ConfigQuery.builder().type(cn.chenjun.cloud.common.util.Constant.ConfigType.DEFAULT).build(), ConfigQuery.builder().type(cn.chenjun.cloud.common.util.Constant.ConfigType.HOST).id(host.getHostId()).build());
                host.setTotalCpu((int) (host.getTotalCpu() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_CPU)));
                host.setTotalMemory((long) (host.getTotalMemory() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_MEMORY)));
            }
            //获取满足的主机列表
            list = list.stream().filter(t -> hostVerify(t, cpu, memory, arch)).collect(Collectors.toList());
            Collections.shuffle(list);
            HostEntity host = null;
            if (hostId > 0) {
                host = list.stream().filter(t -> Objects.equals(t.getHostId(), hostId)).findFirst().orElse(null);
            }

            if (host == null) {
                if (cpu > 0 && memory > 0) {
                    Map<Integer, Float> scoreMap = new HashMap<>(list.size());
                    for (HostEntity entity : list) {
                        List<ConfigQuery> queryList = Arrays.asList(ConfigQuery.builder().type(cn.chenjun.cloud.common.util.Constant.ConfigType.DEFAULT).id(0).build(), ConfigQuery.builder().type(cn.chenjun.cloud.common.util.Constant.ConfigType.DEFAULT).id(entity.getHostId()).build());
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
                host = list.stream().findFirst().orElseThrow(() -> new CodeException(ErrorCode.HOST_NOT_RESOURCE, "没有可用的主机资源，cpu=" + cpu + ", memory=" + memory));
            }
            return host;
        }
    }

    private boolean hostVerify(HostEntity host, int cpu, long memory, String arch) {
        if (!Objects.equals(host.getStatus(), cn.chenjun.cloud.common.util.Constant.HostStatus.ONLINE)) {
            return false;
        }
        if (!ObjectUtils.isEmpty(arch) && !arch.equalsIgnoreCase(host.getArch())) {
            return false;
        }
        int allocateCpu = host.getAllocationCpu() + cpu;
        long allocationMemory = host.getAllocationMemory() + memory;
        return host.getTotalCpu() > allocateCpu && host.getTotalMemory() > allocationMemory;
    }

    @Transactional(rollbackFor = Exception.class)
    public void initHostAllocate() {
        List<HostEntity> hosts = this.hostDao.listAll();
        for (HostEntity host : hosts) {
            List<GuestEntity> guestList = this.guestDao.listRunningByHostId(host.getHostId());
            if (guestList == null) {
                host.setAllocationCpu(0);
                host.setAllocationMemory(0L);
            } else {
                host.setAllocationCpu(guestList.stream().mapToInt(GuestEntity::getCpu).sum());
                host.setAllocationMemory(guestList.stream().mapToLong(GuestEntity::getMemory).sum());
            }
        }
    }
}
