package com.roamblue.cloud.agent.service;

import com.roamblue.cloud.common.agent.HostModel;

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
