package cn.roamblue.cloud.agent.task;

import cn.roamblue.cloud.agent.operate.OsOperate;
import cn.roamblue.cloud.agent.operate.impl.OsOperateImpl;
import cn.roamblue.cloud.agent.service.impl.ConnectPool;
import cn.roamblue.cloud.common.agent.OsRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Command;
import cn.roamblue.cloud.common.util.ErrorCode;
import org.libvirt.Connect;

/**
 * @author chenjun
 */
public class OsRunner extends AbstractTaskRunner<OsRequest, Void> {

    private final OsOperate operate = new OsOperateImpl();

    public OsRunner(ConnectPool connectPool) {
        super(connectPool);
    }

    @Override
    protected Void run(Connect connect, OsRequest request) throws Exception {

        if (Command.Os.DESTROY.equals(request.getCommand())) {
            this.operate.destroy(connect, request.getDestroy());
        } else if (Command.Os.START.equals(request.getCommand())) {
            this.operate.start(connect, request.getStart());
        } else if (Command.Os.REBOOT.equals(request.getCommand())) {
            this.operate.reboot(connect, request.getReboot());
        } else if (Command.Os.SHUTDOWN.equals(request.getCommand())) {
            this.operate.shutdown(connect, request.getShutdown());
        } else if (Command.Os.QMA.equals(request.getCommand())) {
            this.operate.qma(connect, request.getQma());
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的虚拟机操作:" + request.getCommand());
        }
        return null;

    }
}
