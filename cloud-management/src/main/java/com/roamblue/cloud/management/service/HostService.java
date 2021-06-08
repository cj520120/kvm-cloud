package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.HostInfo;

import java.util.List;

public interface HostService {
    /**
     * @return
     */
    List<HostInfo> listHost();

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
     * @return
     */
    HostInfo createHost(int clusterId, String name, String ip, String uri);

    /**
     * 销毁
     *
     * @return
     */
    void destroyHostById(int id);

}
