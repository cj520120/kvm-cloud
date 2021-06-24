package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.ClusterInfo;

import java.util.List;

public interface ClusterUiService {
    /**
     * 获取集群列表
     *
     * @return
     */
    ResultUtil<List<ClusterInfo>> listCluster();

    /**
     * 根据ID查询集群信息
     *
     * @param id
     * @return
     */
    ResultUtil<ClusterInfo> findClusterById(int id);

    /**
     * 创建集群信息
     *
     * @param name
     * @param overCpu
     * @param overMemory
     * @return
     */
    ResultUtil<ClusterInfo> createCluster(String name, float overCpu, float overMemory);

    /**
     * 修改集群信息
     *
     * @param id
     * @param name
     * @param overCpu
     * @param overMemory
     * @return
     */
    ResultUtil<ClusterInfo> modifyCluster(int id, String name, float overCpu, float overMemory);

    /**
     * 销毁集群信息
     *
     * @param id
     * @return
     */
    ResultUtil<Void> destroyClusterById(int id);
}
