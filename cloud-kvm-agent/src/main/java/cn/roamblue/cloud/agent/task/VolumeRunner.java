package cn.roamblue.cloud.agent.task;

import cn.roamblue.cloud.agent.operate.VolumeOperate;
import cn.roamblue.cloud.agent.operate.impl.VolumeOperateImpl;
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

    private final VolumeOperate operate = new VolumeOperateImpl();
    public VolumeRunner(ConnectPool connectPool) {
        super(connectPool);
    }

    @Override
    protected VolumeModel run(Connect connect, VolumeRequest request) throws Exception {

            if (Command.Volume.CREATE.equals(request.getCommand())) {
                return this.operate.create(connect, request.getCreate());
            } else if (Command.Volume.DESTROY.equals(request.getCommand())) {
                this.operate.destroy(connect, request.getDestroy());
            } else if (Command.Volume.RESIZE.equals(request.getCommand())) {
                return this.operate.resize(connect, request.getResize());
            } else if (Command.Volume.CLONE.equals(request.getCommand())) {
                return this.operate.clone(connect, request.getClone());
            } else if (Command.Volume.MIGRATE.equals(request.getCommand())) {
                return this.operate.migrate(connect, request.getMigrate());
            } else if (Command.Volume.SNAPSHOT.equals(request.getCommand())) {
                return this.operate.snapshot(connect, request.getSnapshot());
            } else if (Command.Volume.TEMPLATE.equals(request.getCommand())) {
                return this.operate.template(connect, request.getTemplate());
            } else if (Command.Volume.DOWNLOAD.equals(request.getCommand())) {
                return this.operate.download(connect, request.getDownload());
            } else {
                throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的存储池操作:" + request.getCommand());
            }
            return null;

    }
}
