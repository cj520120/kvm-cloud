package cn.roamblue.cloud.agent.operate;

import cn.roamblue.cloud.common.agent.NetworkRequest;
import org.libvirt.Connect;

/**
 * @author chenjun
 */
public interface NetworkOperate {
    /**
     * 初始化网络信息
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    void create(Connect connect, NetworkRequest request) throws Exception;

    /**
     * 删除网络信息
     * @param connect
     * @param name
     * @throws Exception
     */
    void destroy(Connect connect,String name) throws Exception;
}
