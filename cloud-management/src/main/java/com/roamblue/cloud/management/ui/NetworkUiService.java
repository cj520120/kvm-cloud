package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.NetworkInfo;
import com.roamblue.cloud.management.bean.VmNetworkInfo;

import java.util.List;

public interface NetworkUiService {
    ResultUtil<List<NetworkInfo>> listNetworks();

    ResultUtil<List<NetworkInfo>> search(int clusterId);

    ResultUtil<List<VmNetworkInfo>> findInstanceNetworkByVmId(int vmId);

    ResultUtil<NetworkInfo> findNetworkById(int id);


    ResultUtil<NetworkInfo> createNetwork(String name, int clusterId, String guestStartIp, String guestEndIp, String managerStartIp, String managerEndIp, String subnet, String gateway, String dns, String card, String type);


    ResultUtil<Void> destroyNetworkById(int id);


    ResultUtil<NetworkInfo> startNetwork(int id);

    ResultUtil<NetworkInfo> pauseNetwork(int id);
}
