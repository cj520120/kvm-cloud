package cn.roamblue.cloud.agent.service.impl;

import cn.roamblue.cloud.agent.service.KvmHostService;
import cn.roamblue.cloud.common.bean.HostInfo;
import org.springframework.stereotype.Service;

/**
 * @author chenjun
 */
@Service
public class KvmHostServiceImpl extends AbstractKvmService implements KvmHostService {

    @Override
    public HostInfo getHostInfo() {

        return super.execute(connect -> HostInfo.builder().hostName(connect.getHostName())
                .version(connect.getVersion())
                .uri(connect.getURI())
                .memory(connect.nodeInfo().memory)
                .cpu(connect.nodeInfo().cpus)
                .hypervisor(connect.getType()).build());
    }
}
