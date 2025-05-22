package cn.chenjun.cloud.management.websocket.cluster.process.component;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.model.NatModel;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.manager.ComponentClientManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chenjun
 */
@Component
public class UpdateComponentNatProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private NetworkService networkService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<List<NatModel>> resultUtil = this.networkService.listComponentNat(msg.getId());
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            NotifyData<List<NatModel>> sendMsg = NotifyData.<List<NatModel>>builder().id(msg.getId()).type(Constant.NotifyType.COMPONENT_UPDATE_NAT).data(resultUtil.getData()).version(System.currentTimeMillis()).build();
            ComponentClientManager.send(msg.getId(), sendMsg);
        }
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_COMPONENT_NAT;
    }
}
