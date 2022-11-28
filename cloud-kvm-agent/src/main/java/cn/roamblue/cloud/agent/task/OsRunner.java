package cn.roamblue.cloud.agent.task;

import cn.roamblue.cloud.agent.service.impl.ConnectPool;
import cn.roamblue.cloud.common.agent.OsRequest;
import cn.roamblue.cloud.common.agent.VolumeModel;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Command;
import cn.roamblue.cloud.common.util.ErrorCode;
import org.libvirt.Connect;

/**
 * @author chenjun
 */
public class OsRunner extends AbstractTaskRunner<OsRequest, VolumeModel> {

    public OsRunner(ConnectPool connectPool) {
        super(connectPool);
    }

    @Override
    protected VolumeModel run(Connect connect, OsRequest request) throws Exception {
        String key = "Guest." + request.getName();
        synchronized (key.intern()) {
            if (Command.Os.DESTROY.equals(request.getCommand())) {

            } else if (Command.Os.START.equals(request.getCommand())) {

            } else if (Command.Os.STOP.equals(request.getCommand())) {

            } else if (Command.Os.REBOOT.equals(request.getCommand())) {

            } else if (Command.Os.SHUTDOWN.equals(request.getCommand())) {

            } else if (Command.Os.QMA.equals(request.getCommand())) {

            } else {
                throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的虚拟机操作:" + request.getCommand());
            }
            return null;
        }
    }
}
