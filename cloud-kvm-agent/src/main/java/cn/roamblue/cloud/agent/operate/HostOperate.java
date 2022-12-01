package cn.roamblue.cloud.agent.operate;

import cn.roamblue.cloud.common.agent.HostModel;
import org.libvirt.Connect;

public interface HostOperate {
    /**
     * 获取主机信息
     * @param connect
     * @return
     * @throws Exception
     */
    HostModel getHostInfo(Connect connect) throws Exception;
}
