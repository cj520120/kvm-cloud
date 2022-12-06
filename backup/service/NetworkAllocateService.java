package cn.roamblue.cloud.management.service;

import cn.roamblue.cloud.management.bean.VmNetworkInfo;

/**
 * @author chenjun
 */
public interface NetworkAllocateService {
    /**
     * 申请管理端地址
     *
     * @param networkId
     * @param vmId
     * @return
     */
    VmNetworkInfo allocateManagerAddress(int networkId, int vmId);

    /**
     * 申请客户端地址
     *
     * @param networkId
     * @param vmId
     * @return
     */
    VmNetworkInfo allocateGuestAddress(int networkId, int vmId);

    /**
     * 获取网络类型
     *
     * @return
     */
    String getType();
}
