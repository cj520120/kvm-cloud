package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.SnapshotModel;
import cn.chenjun.cloud.management.servcie.VolumeService;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateSnapshotProcess extends AbstractClusterMessageProcess {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private VolumeService volumeService;

    @Override
    public void process(NotifyData<?> msg) {
        ResultUtil<SnapshotModel> resultUtil = this.volumeService.getSnapshotInfo(msg.getId());
        NotifyData<ResultUtil<SnapshotModel>> sendMsg = NotifyData.<ResultUtil<SnapshotModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_SNAPSHOT).data(resultUtil).version(System.currentTimeMillis()).build();
        wsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_SNAPSHOT;
    }
}
