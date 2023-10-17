package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.config.ApplicationConfig;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class AllocateService extends AbstractService {
    @Autowired
    private ApplicationConfig applicationConfig;

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    public StorageEntity allocateStorage(int storageId) {
        StorageEntity storage;
        if (storageId > 0) {
            storage = storageMapper.selectById(storageId);
            if (storage == null) {
                throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
            }
        } else {
            List<StorageEntity> storageList = storageMapper.selectList(new QueryWrapper<>());
            storageList = storageList.stream().filter(t -> Objects.equals(t.getStatus(), Constant.StorageStatus.READY)).collect(Collectors.toList());
            storage = storageList.stream().min((o1, o2) -> Long.compare(o2.getAvailable(), o1.getAvailable())).orElseThrow(() -> new CodeException(ErrorCode.STORAGE_NOT_SPACE, "没有可用的存储池资源"));
        }
        return storage;
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    public GuestNetworkEntity allocateNetwork(int networkId) {
        QueryWrapper<GuestNetworkEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("network_id", networkId);
        wrapper.eq("guest_id", 0);
        wrapper.last("limit 0,1");
        GuestNetworkEntity guestNetwork = guestNetworkMapper.selectOne(wrapper);
        if (guestNetwork == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_SPACE, "没有可用的网络资源");
        }
        return guestNetwork;
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    public HostEntity allocateHost(int hostId, int mustHostId, int cpu, long memory) {
        if (mustHostId > 0) {
            HostEntity host = this.hostMapper.selectById(mustHostId);
            host.setTotalCpu((int) (host.getTotalCpu() * applicationConfig.getOverCpu()));
            host.setTotalMemory((long) (host.getTotalMemory() * applicationConfig.getOverMemory()));
            if (!hostVerify(host, cpu, memory)) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "主机没有可用资源");
            }
            return host;
        } else {
            List<HostEntity> list = this.hostMapper.selectList(new QueryWrapper<>());
            for (HostEntity host : list) {
                host.setTotalCpu((int) (host.getTotalCpu() * applicationConfig.getOverCpu()));
                host.setTotalMemory((long) (host.getTotalMemory() * applicationConfig.getOverMemory()));
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

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    public void initHostAllocate() {
        List<HostEntity> hosts = this.hostMapper.selectList(new QueryWrapper<>());
        Map<Integer, List<GuestEntity>> map = guestMapper.selectList(new QueryWrapper<GuestEntity>().gt("host_id", 0)).stream().collect(Collectors.groupingBy(GuestEntity::getHostId));
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
