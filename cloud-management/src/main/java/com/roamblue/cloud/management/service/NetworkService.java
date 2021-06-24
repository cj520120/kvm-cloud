package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.NetworkInfo;
import com.roamblue.cloud.management.bean.VmNetworkInfo;

import java.util.List;

public interface NetworkService {
    /**
     * 网络列表
     *
     * @return
     */
    List<NetworkInfo> listNetwork();

    /**
     * 搜索
     *
     * @param clusterId
     * @return
     */
    List<NetworkInfo> search(int clusterId);

    /**
     * 根据集群获取网络信息
     *
     * @return
     */
    List<NetworkInfo> listNetworkByClusterId(int clusterId);

    /**
     * 根据ID获取网络信息
     *
     * @param id
     * @return
     */
    List<VmNetworkInfo> findVmNetworkByVmId(int id);


    /**
     * 解除网络绑定
     *
     * @param vmId
     * @return
     */
    void unBindVmNetworkByVmId(int vmId);

    /**
     * 根据ID获取网络信息
     *
     * @param id
     * @return
     */
    NetworkInfo findNetworkById(int id);

    /**
     * 获取网络对IP地址列表
     *
     * @param networkId
     * @return
     */
    List<VmNetworkInfo> listVmNetworkByNetworkId(int networkId);

    /**
     * 创建网络
     *
     * @return
     */
    NetworkInfo createNetwork(String name, int clusterId, String managerStartIp, String managerEndIp, String guestStartIp, String guestEndIp, String subnet, String gateway, String dns, String card, String type);

    /**
     * 销毁网络
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
    NetworkInfo pauseNetworkById(int id);
}
