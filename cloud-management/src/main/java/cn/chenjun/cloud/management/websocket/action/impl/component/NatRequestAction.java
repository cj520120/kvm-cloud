package cn.chenjun.cloud.management.websocket.action.impl.component;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.base.MapData;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.NatEntity;
import cn.chenjun.cloud.management.model.NatModel;
import cn.chenjun.cloud.management.servcie.ConvertService;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.listen.context.ComponentContext;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
public class NatRequestAction implements WsAction<MapData> {
    @Autowired
    private NetworkService networkService;
    @Autowired
    private ConvertService convertService;

    @Override
    public void doAction(Client webSocket, WsMessage<MapData> msg) throws IOException {
        ComponentContext context = (ComponentContext) webSocket.getContext();
        if (context == null) {
            webSocket.close();
            return;
        }
        List<NatEntity> natList = this.networkService.listComponentNat(context.getComponentId());
        List<NatModel> natModels = natList.stream().map(convertService::initNatModel).collect(Collectors.toList());
        NotifyData<List<NatModel>> sendMsg = NotifyData.<List<NatModel>>builder().type(Constant.NotifyType.NOTIFY_NAT_UPDATE).data(natModels).build();
        WsMessage<NotifyData<List<NatModel>>> wsMessage = WsMessage.<NotifyData<List<NatModel>>>builder().command(Constant.SocketCommand.COMPONENT_NOTIFY).data(sendMsg).build();
        webSocket.sendJsonPacket(wsMessage);
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.COMPONENT_NAT_REQUEST;
    }
}
