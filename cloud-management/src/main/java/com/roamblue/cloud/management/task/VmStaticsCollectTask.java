package com.roamblue.cloud.management.task;

import cn.hutool.cache.impl.LRUCache;
import com.roamblue.cloud.common.agent.VmStaticsModel;
import com.roamblue.cloud.management.data.entity.HostEntity;
import com.roamblue.cloud.management.data.entity.VmEntity;
import com.roamblue.cloud.management.data.entity.VmStaticsEntity;
import com.roamblue.cloud.management.data.mapper.HostMapper;
import com.roamblue.cloud.management.data.mapper.VmMapper;
import com.roamblue.cloud.management.data.mapper.VmStatsMapper;
import com.roamblue.cloud.management.service.AgentService;
import com.roamblue.cloud.management.service.LockService;
import com.roamblue.cloud.management.util.HostStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * VM统计收集
 */
@Component
public class VmStaticsCollectTask extends AbstractTask {
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private LockService lockService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private VmMapper vmMapper;
    @Autowired
    private VmStatsMapper vmStatsMapper;

    private LRUCache<Integer, VmStaticsEntity> cache = new LRUCache<>(100000, 60 * 1000);

    @Override
    protected int getInterval() {
        return 5000;
    }

    @Override
    protected String getName() {
        return "VmStaticsCheckTask";
    }

    @Override
    protected void call() {
        List<HostEntity> list = hostMapper.selectAll().stream().filter(t -> t.getHostStatus().equals(HostStatus.READY)).collect(Collectors.toList());
        if (list == null || list.isEmpty()) {
            return;
        }
        for (HostEntity hostInfo : list) {
            List<VmStaticsModel> instanceStateList = agentService.listVmStatics(hostInfo.getHostUri()).getData();
            if (instanceStateList == null || instanceStateList.isEmpty()) {
                continue;
            }
            long now = System.currentTimeMillis();
            for (VmStaticsModel instanceStaticsResponse : instanceStateList) {
                VmEntity instanceEntity = vmMapper.findByName(instanceStaticsResponse.getName());
                if (instanceEntity == null) {
                    continue;
                }
                VmStaticsEntity statsEntity = VmStaticsEntity.builder()
                        .createTime(new Date(now))
                        .vmId(instanceEntity.getId())
                        .diskWriteSpeed(instanceStaticsResponse.getDiskWriteSpeed())
                        .diskReadSpeed(instanceStaticsResponse.getDiskReadSpeed())
                        .networkSendSpeed(instanceStaticsResponse.getNetworkSendSpeed())
                        .networkReceiveSpeed(instanceStaticsResponse.getNetworkReceiveSpeed())
                        .cpuUsage(instanceStaticsResponse.getCpuUsage())
                        .build();
                vmStatsMapper.insert(statsEntity);
            }
        }

    }
}
