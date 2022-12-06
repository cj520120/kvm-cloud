package cn.roamblue.cloud.management.ui;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.bean.NetworkInfo;
import cn.roamblue.cloud.management.bean.VmNetworkInfo;

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
     * @param managerStartIp
     * @param managerEndIp
     * @param guestStartIp
     * @param guestEndIp
     * @param subnet
     * @param gateway
     * @param dns
     * @param card
     * @param type
     * @return
     */
    ResultUtil<NetworkInfo> createNetwork(String name, int clusterId, String managerStartIp, String managerEndIp, String guestStartIp, String guestEndIp, String subnet, String gateway, String dns, String card, String type);

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
