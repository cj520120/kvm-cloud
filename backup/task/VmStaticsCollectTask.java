package cn.roamblue.cloud.management.task;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.roamblue.cloud.common.agent.VmStaticsModel;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.VmEntity;
import cn.roamblue.cloud.management.data.entity.VmStaticsEntity;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.data.mapper.VmMapper;
import cn.roamblue.cloud.management.data.mapper.VmStatsMapper;
import cn.roamblue.cloud.management.service.AgentService;
import cn.roamblue.cloud.management.util.HostStatus;

/**
 * VM统计收集
 *
 * @author chenjun
 */
@Component
public class VmStaticsCollectTask extends AbstractTask {
    @Autowired
    private HostMapper hostMapper; 
    @Autowired
    private AgentService agentService;
    @Autowired
    private VmMapper vmMapper;
    @Autowired
    private VmStatsMapper vmStatsMapper; 

    @Override
    protected int getInterval() {
        return this.config.getVmStatsCheckInterval();
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
