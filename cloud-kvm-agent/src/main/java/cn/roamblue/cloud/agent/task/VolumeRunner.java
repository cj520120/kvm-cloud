package cn.roamblue.cloud.agent.task;

import cn.roamblue.cloud.agent.service.impl.ConnectPool;
import cn.roamblue.cloud.common.agent.VolumeModel;
import cn.roamblue.cloud.common.agent.VolumeRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Command;
import cn.roamblue.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;

/**
 * @author chenjun
 */
@Slf4j
public class VolumeRunner extends AbstractTaskRunner<VolumeRequest, VolumeModel> {

    public VolumeRunner(ConnectPool connectPool) {
        super(connectPool);
    }

    @Override
    protected VolumeModel run(Connect connect, VolumeRequest request) throws Exception {
        String key = "Volume." + request.getName();
        synchronized (key.intern()) {
            if (Command.Volume.CREATE.equals(request.getCommand())) {
                /**
                 * 1、父文件格式只能为qcow2格式
                 * 2、如果文件不为qcow格式，则在存在父文件的情况下直接clone过来，否则利用backupfile
                 */

            } else if (Command.Volume.DESTROY.equals(request.getCommand())) {

            } else if (Command.Volume.RESIZE.equals(request.getCommand())) {

            } else if (Command.Volume.CLONE.equals(request.getCommand())) {

            } else if (Command.Volume.MIGRATE.equals(request.getCommand())) {

            } else if (Command.Volume.SNAPSHOT.equals(request.getCommand())) {

            } else if (Command.Volume.TEMPLATE.equals(request.getCommand())) {

            } else if (Command.Volume.DOWNLOAD.equals(request.getCommand())) {

            } else {
                throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的存储池操作:" + request.getCommand());
            }
            return null;
        }
    }
}
