package cn.chenjun.cloud.management.websocket.action.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.NatModel;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.client.WsClient;
import cn.chenjun.cloud.management.websocket.client.owner.ComponentWsOwner;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import cn.chenjun.cloud.management.websocket.util.WsSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.List;

/**
 * @author chenjun
 */
@Component
public class NatRequestAction implements WsAction {
    @Autowired
    private NetworkService networkService;

    @Override
    public void doAction(Session session, WsRequest msg) throws IOException {
        WsClient<ComponentWsOwner> client = WsSessionManager.getClient(session);
        if (client == null) {
            return;
        }
        ResultUtil<List<NatModel>> resultUtil = this.networkService.listComponentNat(client.getOwner().getComponentId());
        NotifyData<List<NatModel>> sendMsg = NotifyData.<List<NatModel>>builder().type(Constant.NotifyType.COMPONENT_UPDATE_NAT).data(resultUtil.getData()).build();
        WsMessage<NotifyData<List<NatModel>>> wsMessage = WsMessage.<NotifyData<List<NatModel>>>builder().command(Constant.SocketCommand.COMPONENT_NOTIFY).data(sendMsg).build();
        session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(wsMessage));
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.COMPONENT_NAT_REQUEST;
    }
}
