package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.ClusterInfo;

import java.util.List;

public interface ClusterService {
    /**
     * 获取集群列表
     *
     * @return
     */
    List<ClusterInfo> listCluster();

    /**
     * 根据ID获取集群
     *
     * @return
     */
    ClusterInfo findClusterById(int id);

    /**
     * 创建集群
     *
     * @return
     */
    ClusterInfo createCluster(String name, float overCpu, float overMemory);

    /**
     * 创建集群
     *
     * @return
     */
    ClusterInfo modifyCluster(int id, String name, float overCpu, float overMemory);

    /**
     * 销毁集群
     *
     * @param id
     * @return
     */
    void destroyClusterById(int id);
}
