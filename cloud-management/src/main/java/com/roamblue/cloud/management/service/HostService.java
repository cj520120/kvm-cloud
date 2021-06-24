package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.HostInfo;

import java.util.List;

/**
 * @author chenjun
 */
public interface HostService {
    /**
     * 获取所有主机列表
     *
     * @return
     */
    List<HostInfo> listHost();

    /**
     * 查询
     *
     * @param clusterId
     * @return
     */
    List<HostInfo> search(int clusterId);


    /**
     * 根据ID获取
     *
     * @param id
     * @return
     */
    HostInfo findHostById(int id);

    /**
     * 创建
     *
     * @param clusterId
     * @param name
     * @param ip
     * @param uri
     * @return
     */
    HostInfo createHost(int clusterId, String name, String ip, String uri);

    /**
     * 销毁
     *
     * @param id
     * @return
     */
    void destroyHostById(int id);

}
