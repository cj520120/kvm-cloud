package cn.roamblue.cloud.management.task;

import cn.roamblue.cloud.common.agent.HostModel;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.service.AgentService;
import cn.roamblue.cloud.management.util.HostStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 更新主机信息
 *
 * @author chenjun
 */ 
@Component
@Slf4j
public class HostCheckTask extends AbstractTask {

    @Autowired
    private AgentService agentService;

    @Autowired
    private HostMapper hostMapper;
    @Override
    protected int getInterval() {
        return this.config.getHostCheckInterval();
    }

    @Override
    protected String getName() {
        return "HostCheckTask";
    }

    @Override
    protected void call() {
        List<HostEntity> hosts = hostMapper.selectAll().stream().filter(t -> t.getHostStatus().equals(HostStatus.READY)).collect(Collectors.toList());
        Collections.shuffle(hosts);
        for (HostEntity host : hosts) {
            ResultUtil<HostModel> resultUtil = agentService.getHostInfo(host.getHostUri());
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                HostModel cloudHostInfo = resultUtil.getData();
                if (!host.getHostCpu().equals(cloudHostInfo.getCpu()) || !host.getHostMemory().equals(cloudHostInfo.getMemory())) {
                    HostEntity update = HostEntity.builder()
                            .id(host.getId())
                            .hostCpu(cloudHostInfo.getCpu())
                            .hostMemory(cloudHostInfo.getMemory())
                            .build();
                    hostMapper.updateById(update);
                    log.info("update host info.uri={} cpu={} memory={}",host.getHostUri(),cloudHostInfo.getCpu(),cloudHostInfo.getMemory());
                    
                }
            }
        }

    }
}
