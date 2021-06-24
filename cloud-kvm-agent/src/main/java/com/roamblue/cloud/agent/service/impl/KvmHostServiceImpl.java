package com.roamblue.cloud.agent.service.impl;

import com.roamblue.cloud.agent.service.KvmHostService;
import com.roamblue.cloud.common.agent.HostModel;
import org.springframework.stereotype.Service;

/**
 * @author chenjun
 */
@Service
public class KvmHostServiceImpl extends AbstractKvmService implements KvmHostService {

    @Override
    public HostModel getHostInfo() {

        return super.excute(connect -> HostModel.builder().hostName(connect.getHostName())
                .version(connect.getVersion())
                .uri(connect.getURI())
                .memory(connect.nodeInfo().memory)
                .cpu(connect.nodeInfo().cpus)
                .hypervisor(connect.getType()).build());
    }
}
