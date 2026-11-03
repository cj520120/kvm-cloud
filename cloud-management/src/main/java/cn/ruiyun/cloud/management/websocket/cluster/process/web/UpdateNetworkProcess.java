package cn.ruiyun.cloud.management.websocket.cluster.process.web;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.model.NetworkModel;
import cn.ruiyun.cloud.management.servcie.ConvertService;
import cn.ruiyun.cloud.management.servcie.NetworkService;
import cn.ruiyun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.ruiyun.cloud.management.websocket.manager.WebClientManager;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateNetworkProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private NetworkService networkService;
    @Autowired
    private ConvertService convertService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<NetworkModel> resultUtil = getResourceData(() -> this.networkService.getNetworkInfo(msg.getId()), this.convertService::initNetworkModel);
        NotifyData<ResultUtil<NetworkModel>> sendMsg = NotifyData.<ResultUtil<NetworkModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_NETWORK).data(resultUtil).version(System.currentTimeMillis()).build();
        WebClientManager.sendAll(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_NETWORK;
    }
}
