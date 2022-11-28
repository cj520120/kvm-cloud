package cn.roamblue.cloud.agent.operate.impl;

import cn.roamblue.cloud.agent.operate.OsOperate;
import cn.roamblue.cloud.common.agent.OsRequest;
import org.libvirt.Connect;

public class OsOperateImpl implements OsOperate {
    @Override
    public void start(Connect connect, OsRequest.Start request) throws Exception {

    }

    @Override
    public void shutdown(Connect connect, OsRequest.Shutdown request) throws Exception {

    }

    @Override
    public void reboot(Connect connect, OsRequest.Reboot request) throws Exception {

    }

    @Override
    public void qma(Connect connect, OsRequest.Qma request) throws Exception {

    }

    @Override
    public void destroy(Connect connect, OsRequest.Destroy request) throws Exception {

    }
}
