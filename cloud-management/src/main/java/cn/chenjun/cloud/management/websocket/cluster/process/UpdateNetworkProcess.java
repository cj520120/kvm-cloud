package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.NetworkModel;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateNetworkProcess extends AbstractClusterMessageProcess {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private NetworkService networkService;

    @Override
    public void process(NotifyData<?> msg) {
        ResultUtil<NetworkModel> resultUtil = this.networkService.getNetworkInfo(msg.getId());
        NotifyData<ResultUtil<NetworkModel>> sendMsg = NotifyData.<ResultUtil<NetworkModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_NETWORK).data(resultUtil).build();
        wsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_NETWORK;
    }
}
