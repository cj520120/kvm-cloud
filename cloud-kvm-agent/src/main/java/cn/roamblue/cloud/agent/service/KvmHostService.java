package cn.roamblue.cloud.agent.service;

import cn.roamblue.cloud.common.agent.HostModel;

/**
 * @author chenjun
 */
public interface KvmHostService {
    /**
     * 获取主机信息
     *
     * @return
     */
    HostModel getHostInfo();
}
