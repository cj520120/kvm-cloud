package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.HostInfo;

import java.util.List;

/**
 * @author chenjun
 */
public interface HostUiService {
    /**
     * 获取主机列表
     *
     * @return
     */
    ResultUtil<List<HostInfo>> listHost();

    /**
     * 搜索主机
     *
     * @param clusterId
     * @return
     */
    ResultUtil<List<HostInfo>> search(int clusterId);

    /**
     * 更具ID获取主机信息
     *
     * @param id
     * @return
     */
    ResultUtil<HostInfo> findHostById(int id);

    /**
     * 创建主机
     *
     * @param clusterId
     * @param name
     * @param ip
     * @param uri
     * @return
     */
    ResultUtil<HostInfo> createHost(int clusterId, String name, String ip, String uri);

    /**
     * 销毁主机
     *
     * @param id
     * @return
     */
    ResultUtil<Void> destroyHostById(int id);
}
