package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.ClusterInfo;

import java.util.List;

public interface ClusterUiService {
    ResultUtil<List<ClusterInfo>> listCluster();

    ResultUtil<ClusterInfo> findClusterById(int id);

    ResultUtil<ClusterInfo> createCluster(String name, float overCpu, float overMemory);

    ResultUtil<ClusterInfo> modifyCluster(int id, String name, float overCpu, float overMemory);

    ResultUtil<Void> destroyClusterById(int id);
}
