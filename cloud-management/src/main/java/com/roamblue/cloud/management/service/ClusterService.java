package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.ClusterInfo;

import java.util.List;

/**
 * @author chenjun
 */
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
     * @param id
     * @return
     */
    ClusterInfo findClusterById(int id);

    /**
     * 创建集群
     *
     * @param name
     * @param overCpu
     * @param overMemory
     * @return
     */
    ClusterInfo createCluster(String name, float overCpu, float overMemory);

    /**
     * 修改集群
     *
     * @param id
     * @param name
     * @param overCpu
     * @param overMemory
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
