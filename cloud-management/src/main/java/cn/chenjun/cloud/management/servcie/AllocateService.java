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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
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
                boolean isSupport =!Objects.equals(t.getType(), cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL);//本地磁盘不参与自动分配
                isSupport =isSupport &&(t.getSupportCategory().intValue() & category) == category;
                return isSupport && Objects.equals(t.getStatus(), Constant.StorageStatus.READY);
            }).collect(Collectors.toList());
            storage = storageList.stream().min((o1, o2) -> Long.compare(o2.getAvailable(), o1.getAvailable())).orElseThrow(() -> new CodeException(ErrorCode.STORAGE_NOT_SPACE, "没有可用的存储池资源"));
        }
        return storage;
    }

    public GuestNetworkEntity allocateNetwork(int networkId) {
        QueryWrapper<GuestNetworkEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("network_id", networkId);
        wrapper.eq("allocate_id", 0);
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
            host.setTotalCpu((int) (host.getTotalCpu() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_CLUSTER_OVER_CPU)));
            host.setTotalMemory((long) (host.getTotalMemory() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_CLUSTER_OVER_MEMORY)));
        }
        list = list.stream().filter(t -> hostVerify(t, cpu, memory))
                .collect(Collectors.toList());
        return list;
    }

    public HostEntity allocateHost(int hostId, int mustHostId, int cpu, long memory) {
        if (mustHostId > 0) {
            HostEntity host = this.hostMapper.selectById(mustHostId);
            List<ConfigQuery> queryList = Arrays.asList(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).build(), ConfigQuery.builder().type(Constant.ConfigType.HOST).id(host.getHostId()).build());
            host.setTotalCpu((int) (host.getTotalCpu() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_CLUSTER_OVER_CPU)));
            host.setTotalMemory((long) (host.getTotalMemory() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_CLUSTER_OVER_MEMORY)));
            if (!hostVerify(host, cpu, memory)) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "主机没有可用资源");
            }
            return host;
        } else {
            List<HostEntity> list = this.hostMapper.selectList(new QueryWrapper<>());
            for (HostEntity host : list) {

                List<ConfigQuery> queryList = Arrays.asList(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).build(), ConfigQuery.builder().type(Constant.ConfigType.HOST).id(host.getHostId()).build());
                host.setTotalCpu((int) (host.getTotalCpu() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_CLUSTER_OVER_CPU)));
                host.setTotalMemory((long) (host.getTotalMemory() * (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_CLUSTER_OVER_MEMORY)));
            }
            list = list.stream().filter(t -> hostVerify(t, cpu, memory))
                    .collect(Collectors.toList());
            Collections.shuffle(list);
            HostEntity host = null;
            if (hostId > 0) {
                host = list.stream().filter(t -> Objects.equals(t.getHostId(), hostId)).findFirst().orElse(null);
            }
            if (host == null) {
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
