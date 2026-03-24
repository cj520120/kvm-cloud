package cn.chenjun.cloud.management.websocket.action.impl;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.NatEntity;
import cn.chenjun.cloud.management.model.NatModel;
import cn.chenjun.cloud.management.servcie.ConvertService;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.client.WebSocket;
import cn.chenjun.cloud.management.websocket.client.context.ComponentContext;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
public class NatRequestAction implements WsAction {
    @Autowired
    private NetworkService networkService;
    @Autowired
    private ConvertService convertService;

    @Override
    public void doAction(WebSocket webSocket, WsRequest msg) throws IOException {
        ComponentContext context = (ComponentContext) webSocket.getContext();
        if (context == null) {
            return;
        }
        List<NatEntity> natList = this.networkService.listComponentNat(context.getComponentId());
        List<NatModel> natModels = natList.stream().map(convertService::initNatModel).collect(Collectors.toList());
        NotifyData<List<NatModel>> sendMsg = NotifyData.<List<NatModel>>builder().type(Constant.NotifyType.COMPONENT_UPDATE_NAT).data(natModels).build();
        WsMessage<NotifyData<List<NatModel>>> wsMessage = WsMessage.<NotifyData<List<NatModel>>>builder().command(Constant.SocketCommand.COMPONENT_NOTIFY).data(sendMsg).build();
        webSocket.send(wsMessage);
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.COMPONENT_NAT_REQUEST;
    }
}
