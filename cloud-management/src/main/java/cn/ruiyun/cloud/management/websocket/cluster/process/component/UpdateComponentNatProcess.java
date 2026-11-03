package cn.ruiyun.cloud.management.websocket.cluster.process.component;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.management.model.NatModel;
import cn.ruiyun.cloud.management.servcie.ConvertService;
import cn.ruiyun.cloud.management.servcie.NetworkService;
import cn.ruiyun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.ruiyun.cloud.management.websocket.manager.ComponentClientManager;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
public class UpdateComponentNatProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private NetworkService networkService;
    @Autowired
    private ConvertService convertService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<List<NatModel>> resultUtil = this.getResourceData(() -> this.networkService.listComponentNat(msg.getId()), source -> {
            List<NatModel> natModels = source.stream().map(convertService::initNatModel).collect(Collectors.toList());
            return natModels;
        });
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            NotifyData<List<NatModel>> sendMsg = NotifyData.<List<NatModel>>builder().id(msg.getId()).type(Constant.NotifyType.NOTIFY_NAT_UPDATE).data(resultUtil.getData()).version(System.currentTimeMillis()).build();
            ComponentClientManager.send(msg.getId(), sendMsg, Constant.SocketCommand.COMPONENT_NOTIFY);
        }
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_COMPONENT_NAT;
    }
}
