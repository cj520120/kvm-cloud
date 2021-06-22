package com.roamblue.cloud.management.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.common.util.ErrorCode;
import com.roamblue.cloud.management.data.entity.HostEntity;
import com.roamblue.cloud.management.data.entity.StorageEntity;
import com.roamblue.cloud.management.data.entity.VolumeEntity;
import com.roamblue.cloud.management.data.mapper.StorageMapper;
import com.roamblue.cloud.management.data.mapper.VolumeMapper;
import com.roamblue.cloud.management.service.AgentService;
import com.roamblue.cloud.management.service.AllocateService;
import com.roamblue.cloud.management.service.LockService;
import com.roamblue.cloud.management.util.LockKeyUtil;
import com.roamblue.cloud.management.util.StorageStatus;
import com.roamblue.cloud.management.util.VolumeStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VolumeDestroyTask extends AbstractTask {
    @Autowired
    private VolumeMapper volumeMapper;
    @Autowired
    private AllocateService allocateService;
    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private AgentService agentService;
    @Autowired
    private LockService lockService;

    @Override
    protected int getInterval() {
        return 60000;
    }

    @Override
    protected String getName() {
        return "DestroyVolume";
    }

    @Override
    protected void call() {
        long removeTime = System.currentTimeMillis() - 30 * 60 * 1000;
        QueryWrapper<VolumeEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("volume_status", VolumeStatus.DESTROY);
        wrapper.lt("remove_time", new Date(removeTime));
        List<VolumeEntity> list = volumeMapper.selectList(wrapper);
        if (list.isEmpty()) {
            return;
        }
        Map<Integer, HostEntity> clusterHostMap = new HashMap<>();
        for (VolumeEntity volume : list) {
            StorageEntity storage = this.storageMapper.selectById(volume.getStorageId());
            if (storage != null) {
                if (!storage.getStorageStatus().equalsIgnoreCase(StorageStatus.READY)) {
                    continue;
                }
                try {
                    HostEntity hostEntity = clusterHostMap.computeIfAbsent(volume.getClusterId(), clusterId -> this.allocateService.allocateHost(clusterId, 0, 0, 0));
                    ResultUtil<Void> destroyVolumeResultUtil = this.agentService.destroyVolume(hostEntity.getHostUri(), storage.getStorageTarget(), volume.getVolumeTarget());
                    if (destroyVolumeResultUtil.getCode() != ErrorCode.SUCCESS) {
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            lockService.run(LockKeyUtil.getVolumeLockKey(volume.getId()), () -> volumeMapper.deleteById(volume.getId()), 1, TimeUnit.MINUTES);
        }
    }
}
