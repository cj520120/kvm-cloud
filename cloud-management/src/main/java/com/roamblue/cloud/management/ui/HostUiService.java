package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.HostInfo;

import java.util.List;

public interface HostUiService {
    ResultUtil<List<HostInfo>> listHost();

    ResultUtil<List<HostInfo>> search(int clusterId);

    ResultUtil<HostInfo> findHostById(int id);

    ResultUtil<HostInfo> createHost(int clusterId, String name, String ip, String uri);

    ResultUtil<Void> destroyHostById(int id);
}
