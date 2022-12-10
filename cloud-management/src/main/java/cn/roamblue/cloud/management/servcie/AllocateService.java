package cn.roamblue.cloud.management.servcie;

import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.GuestNetworkEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.mapper.GuestNetworkMapper;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AllocateService {
    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private GuestNetworkMapper guestNetworkMapper;
    @Autowired
    private HostMapper hostMapper;

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
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
            storage = storageList.stream().sorted((o1, o2) -> Long.compare(o2.getAvailable(), o1.getAvailable())).findFirst().orElse(null);
        }
        return storage;
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
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
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public HostEntity allocateHost(int hostId, int mustHostId, int cpu, long memory) {
        if (mustHostId > 0) {
            HostEntity host = this.hostMapper.selectById(mustHostId);
            if (host == null || !Objects.equals(host.getStatus(), Constant.HostStatus.ONLINE)) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "没有可用的主机");
            }
            if (host.getTotalMemory() - host.getAllocationMemory() > memory) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "主机当前可用内存不足");
            }
            if (host.getTotalCpu() - host.getAllocationCpu() > cpu) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "主机当前可用CPU不足");
            }
            return host;
        } else {
            List<HostEntity> list = this.hostMapper.selectList(new QueryWrapper<>());
            list = list.stream().filter(t -> Objects.equals(t.getStatus(), Constant.HostStatus.ONLINE))
//                    .filter(t -> t.getTotalCpu() - t.getAllocationCpu() > cpu && t.getTotalMemory() - t.getAllocationMemory() > memory)
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

}
