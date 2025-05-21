package cn.chenjun.cloud.management.websocket.cluster.process.web;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.NetworkModel;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.management.websocket.util.WsSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateNetworkProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private NetworkService networkService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<NetworkModel> resultUtil = this.networkService.getNetworkInfo(msg.getId());
        NotifyData<ResultUtil<NetworkModel>> sendMsg = NotifyData.<ResultUtil<NetworkModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_NETWORK).data(resultUtil).version(System.currentTimeMillis()).build();
        WsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_NETWORK;
    }
}
