package cn.roamblue.cloud.agent.operate;

import cn.roamblue.cloud.common.agent.NetworkRequest;
import org.libvirt.Connect;

/**
 * @author chenjun
 */
public interface NetworkOperate {
    /**
     * 创建基础网络
     * @param connect
     * @param request
     * @throws Exception
     */
    void createBasic(Connect connect, NetworkRequest request) throws Exception;

    /**
     * 创建Vlan网络
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    void createVlan(Connect connect, NetworkRequest request) throws Exception;

    /**
     * 删除基础网络信息
     * @param connect
     * @param request
     * @throws Exception
     */
    void destroyBasic(Connect connect, NetworkRequest request) throws Exception;
    /**
     * 删除Vlan网络信息
     * @param connect
     * @param request
     * @throws Exception
     */
    void destroyVlan(Connect connect, NetworkRequest request) throws Exception;
}
