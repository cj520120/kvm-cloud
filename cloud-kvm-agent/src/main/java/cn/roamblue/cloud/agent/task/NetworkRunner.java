package cn.roamblue.cloud.agent.task;

import cn.roamblue.cloud.agent.service.impl.ConnectPool;
import cn.roamblue.cloud.common.agent.NetworkRequest;
import cn.roamblue.cloud.common.agent.VolumeModel;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Command;
import cn.roamblue.cloud.common.util.ErrorCode;
import org.libvirt.Connect;

/**
 * @author chenjun
 */
public class NetworkRunner extends AbstractTaskRunner<NetworkRequest, VolumeModel> {

    public NetworkRunner(ConnectPool connectPool) {
        super(connectPool);
    }

    @Override
    protected VolumeModel run(Connect connect, NetworkRequest request) throws Exception {

        if (Command.Network.CREATE.equals(request.getCommand())) {


        } else if (Command.Network.DESTROY.equals(request.getCommand())) {

        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的虚拟机操作:" + request.getCommand());
        }
        return null;
    }
}
