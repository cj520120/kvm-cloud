package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.NetworkInfo;
import com.roamblue.cloud.management.bean.VmNetworkInfo;

import java.util.List;

public interface NetworkService {
    /**
     * @return
     */
    List<NetworkInfo> listNetwork();


    List<NetworkInfo> search(int clusterId);

    /**
     * @return
     */
    List<NetworkInfo> listNetworkByClusterId(int clusterId);

    /**
     * @param id
     * @return
     */
    List<VmNetworkInfo> findVmNetworkByVmId(int id);


    /**
     * @param vmId
     * @return
     */
    void detachVmNetworkByVmId(int vmId);

    /**
     * 根据ID获取
     *
     * @param id
     * @return
     */
    NetworkInfo findNetworkById(int id);

    /**
     * @param networkId
     * @return
     */
    List<VmNetworkInfo> listVmNetworkByNetworkId(int networkId);

    /**
     * 创建
     *
     * @return
     */
    NetworkInfo createNetwork(String name, int clusterId, String managerStartIp, String managerEndIp, String guestStartIp, String guestEndIp, String subnet, String gateway, String dns, String card, String type);

    /**
     * 销毁
     *
     * @return
     */
    void destroyNetworkById(int id);

    /**
     * 启动网络
     *
     * @param id
     */
    NetworkInfo startNetworkById(int id);

    /**
     * 暂停网络
     *
     * @param id
     */
    NetworkInfo stopNetworkById(int id);
}
