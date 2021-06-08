package com.roamblue.cloud.management.task;

import com.roamblue.cloud.common.agent.HostModel;
import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.common.util.ErrorCode;
import com.roamblue.cloud.management.data.entity.HostEntity;
import com.roamblue.cloud.management.data.mapper.HostMapper;
import com.roamblue.cloud.management.data.mapper.StorageMapper;
import com.roamblue.cloud.management.data.mapper.VolumeMapper;
import com.roamblue.cloud.management.service.AgentService;
import com.roamblue.cloud.management.service.HostService;
import com.roamblue.cloud.management.service.LockService;
import com.roamblue.cloud.management.service.StorageService;
import com.roamblue.cloud.management.util.HostStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HostCheckTask extends AbstractTask {

    @Autowired
    private HostService hostService;

    @Autowired
    private StorageService storagePoolService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private VolumeMapper volumeMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private LockService lockService;

    @Override
    protected int getInterval() {
        return 10000;
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
                }
            }
        }

    }
}
