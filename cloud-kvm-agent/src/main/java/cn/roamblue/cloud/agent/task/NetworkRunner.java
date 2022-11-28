package cn.roamblue.cloud.agent.task;

import cn.roamblue.cloud.agent.operate.NetworkOperate;
import cn.roamblue.cloud.agent.operate.impl.NetworkOperateImpl;
import cn.roamblue.cloud.agent.service.impl.ConnectPool;
import cn.roamblue.cloud.common.agent.NetworkRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Command;
import cn.roamblue.cloud.common.util.ErrorCode;
import org.libvirt.Connect;

/**
 * @author chenjun
 */
public class NetworkRunner extends AbstractTaskRunner<NetworkRequest, Void> {

    private static final NetworkOperate operate = new NetworkOperateImpl();

    public NetworkRunner(ConnectPool connectPool) {
        super(connectPool);
    }

    @Override
    protected Void run(Connect connect, NetworkRequest request) throws Exception {

        if (Command.Network.CREATE_BASIC.equals(request.getCommand())) {
            operate.createVlan(connect, request);
        }if (Command.Network.CREATE_VLAN.equals(request.getCommand())) {
            operate.createVlan(connect, request);
        } if (Command.Network.DESTROY_BASIC.equals(request.getCommand())) {
            operate.destroyBasic(connect, request);
        }  else if (Command.Network.DESTROY_VLAN.equals(request.getCommand())) {
            operate.destroyVlan(connect, request);
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的网络操作:" + request.getCommand());
        }
        return null;
    }
}
