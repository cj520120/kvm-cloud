package com.roamblue.cloud.management.service;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.VncInfo;

public interface VncService extends VmService {
    /**
     * 根据集群ID启动VNC
     *
     * @param clusterId
     * @return
     */
    void start(int clusterId);

    /**
     * 增加VNC
     *
     * @param clusterId
     * @param vmId
     * @param host
     * @param port
     * @return
     */
    ResultUtil<Void> register(int clusterId, int vmId, String host, int port, String password);

    /**
     * 删除实例
     *
     * @param clusterId
     * @param vmId
     * @return
     */
    ResultUtil<Void> unRegister(int clusterId, int vmId);

    /**
     * 根据实例查询VNC
     *
     * @param clusterId
     * @param vmId
     * @return
     */
    VncInfo findVncByVmId(Integer clusterId, Integer vmId);
}
