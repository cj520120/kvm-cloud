package cn.roamblue.cloud.management.task;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.roamblue.cloud.common.agent.VolumeModel;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.ClusterEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.data.mapper.ClusterMapper;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.data.mapper.VolumeMapper;
import cn.roamblue.cloud.management.service.AgentService;
import cn.roamblue.cloud.management.service.LockService;
import cn.roamblue.cloud.management.util.ClusterStatus;
import cn.roamblue.cloud.management.util.HostStatus;
import cn.roamblue.cloud.management.util.LockKeyUtil;
import cn.roamblue.cloud.management.util.StorageStatus;
import cn.roamblue.cloud.management.util.VolumeStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * 磁盘检测并更新任务
 *
 * @author chenjun
 */
@Slf4j
@Component
public class VolumeCheckTask extends AbstractTask { 

    @Autowired
    private AgentService agentService;

    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private VolumeMapper volumeMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private ClusterMapper clusterMapper;
    @Autowired
    private LockService lockService;

    @Override
    protected int getInterval() {
        return this.config.getVolumeCheckInterval();
    }

    @Override
    protected String getName() {
        return "VolumeCheckTask";
    }

    @Override
    protected void call() {
        List<ClusterEntity> clusterList = clusterMapper.selectAll().stream().filter(t -> t.getClusterStatus().equals(ClusterStatus.READY)).collect(Collectors.toList());
        for (ClusterEntity clusterEntity : clusterList) {
            List<HostEntity> hosts = hostMapper.findByClusterId(clusterEntity.getId()).stream().filter(t -> t.getHostStatus().equals(HostStatus.READY)).collect(Collectors.toList());

            if (hosts.isEmpty()) {
                continue;
            }
            List<StorageEntity> storageList = storageMapper.findByClusterId(clusterEntity.getId()).stream().filter(t -> t.getStorageStatus().equals(StorageStatus.READY)).collect(Collectors.toList());
            Map<Integer, StorageEntity> storageMap = storageList.stream().collect(Collectors.toMap(StorageEntity::getId, Function.identity()));
            List<VolumeEntity> volumes = volumeMapper.findByClusterId(clusterEntity.getId());
            for (VolumeEntity volume : volumes) {
                if (!volume.getVolumeStatus().equals(VolumeStatus.READY)) {
                    continue;
                }
                lockService.tryRun(LockKeyUtil.getVolumeLockKey(volume.getId()), () -> {
                    StorageEntity storage = storageMap.get(volume.getStorageId());
                    if (storage != null) {
                        Collections.shuffle(hosts);
                        HostEntity host = hosts.get(0);
                        ResultUtil<VolumeModel> volumeResultUtil = agentService.getVolumeInfo(host.getHostUri(), storage.getStorageTarget(), volume.getVolumeTarget());
                        if (volumeResultUtil.getCode() == ErrorCode.SUCCESS) {
                            VolumeModel cloudVolumeInfo = volumeResultUtil.getData();
                            VolumeEntity update = VolumeEntity.builder()
                                    .id(volume.getId())
                                    .volumeAllocation(cloudVolumeInfo.getAllocation())
                                    .volumeCapacity(cloudVolumeInfo.getCapacity())
                                    .build();
                            QueryWrapper<VolumeEntity> queryWrapper = new QueryWrapper<>();
                            queryWrapper.eq("id", volume.getId());
                            queryWrapper.eq("volume_status", volume.getVolumeStatus());
                            volumeMapper.update(update, queryWrapper);
                        } else {
                            log.error("磁盘卷检测失败.volumeId={} storageId={} hostId={} msg={}", volume.getId(), storage.getId(), host.getHostName(), volumeResultUtil.getMessage());
                        }
                    }
                    return null;
                }, 1, TimeUnit.MINUTES);
            }
        }

    }

}
