package cn.chenjun.cloud.management.websocket.cluster.process.web;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.HostModel;
import cn.chenjun.cloud.management.servcie.HostService;
import cn.chenjun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.management.websocket.util.WsSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateHostProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private HostService hostService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<HostModel> resultUtil = this.hostService.getHostInfo(msg.getId());
        NotifyData<ResultUtil<HostModel>> sendMsg = NotifyData.<ResultUtil<HostModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_HOST).data(resultUtil).version(System.currentTimeMillis()).build();
        WsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_HOST;
    }
}
