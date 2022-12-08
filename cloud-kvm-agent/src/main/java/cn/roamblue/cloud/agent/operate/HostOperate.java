package cn.roamblue.cloud.agent.operate;

import cn.roamblue.cloud.common.bean.HostInfo;
import cn.roamblue.cloud.common.bean.InitHostRequest;
import org.libvirt.Connect;

public interface HostOperate {
    /**
     * 获取主机信息
     * @param connect
     * @return
     * @throws Exception
     */
    HostInfo getHostInfo(Connect connect) throws Exception;

    HostInfo initHost(Connect connect, InitHostRequest request) throws Exception;
}
