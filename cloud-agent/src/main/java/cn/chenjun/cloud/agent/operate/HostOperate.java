package cn.chenjun.cloud.agent.operate;

import cn.chenjun.cloud.common.bean.HostInfo;
import cn.chenjun.cloud.common.bean.InitHostRequest;
import org.libvirt.Connect;

/**
 * @author chenjun
 */
public interface HostOperate {
    /**
     * 获取主机信息
     *
     * @param connect
     * @return
     * @throws Exception
     */
    HostInfo getHostInfo(Connect connect) throws Exception;

    /**
     * 初始化主机信息
     *
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    HostInfo initHost(Connect connect, InitHostRequest request) throws Exception;
}
