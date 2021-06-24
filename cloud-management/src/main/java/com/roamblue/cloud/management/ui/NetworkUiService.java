package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.NetworkInfo;
import com.roamblue.cloud.management.bean.VmNetworkInfo;

import java.util.List;

/**
 * @author chenjun
 */
public interface NetworkUiService {
    /**
     * 获取网络列表
     *
     * @return
     */
    ResultUtil<List<NetworkInfo>> listNetworks();

    /**
     * 搜索网络
     *
     * @param clusterId
     * @return
     */
    ResultUtil<List<NetworkInfo>> search(int clusterId);

    /**
     * 获取指定VM的网卡信息
     *
     * @param vmId
     * @return
     */
    ResultUtil<List<VmNetworkInfo>> findInstanceNetworkByVmId(int vmId);

    /**
     * 根据ID获取网络信息
     *
     * @param id
     * @return
     */
    ResultUtil<NetworkInfo> findNetworkById(int id);

    /**
     * 创建网络
     *
     * @param name
     * @param clusterId
     * @param guestStartIp
     * @param guestEndIp
     * @param managerStartIp
     * @param managerEndIp
     * @param subnet
     * @param gateway
     * @param dns
     * @param card
     * @param type
     * @return
     */
    ResultUtil<NetworkInfo> createNetwork(String name, int clusterId, String guestStartIp, String guestEndIp, String managerStartIp, String managerEndIp, String subnet, String gateway, String dns, String card, String type);

    /**
     * 销毁网络
     *
     * @param id
     * @return
     */
    ResultUtil<Void> destroyNetworkById(int id);

    /**
     * 启动网络
     *
     * @param id
     * @return
     */
    ResultUtil<NetworkInfo> startNetwork(int id);

    /**
     * 暂停网络
     *
     * @param id
     * @return
     */
    ResultUtil<NetworkInfo> pauseNetwork(int id);
}
