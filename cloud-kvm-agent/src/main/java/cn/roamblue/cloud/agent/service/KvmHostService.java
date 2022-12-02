package cn.roamblue.cloud.agent.service;

import cn.roamblue.cloud.common.bean.HostInfo;

/**
 * @author chenjun
 */
public interface KvmHostService {
    /**
     * 获取主机信息
     *
     * @return
     */
    HostInfo getHostInfo();
}
