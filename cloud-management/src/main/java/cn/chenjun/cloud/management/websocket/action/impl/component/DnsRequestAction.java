package cn.chenjun.cloud.management.websocket.action.impl.component;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.base.MapData;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.DnsEntity;
import cn.chenjun.cloud.management.model.DnsModel;
import cn.chenjun.cloud.management.servcie.ConvertService;
import cn.chenjun.cloud.management.servcie.DnsService;
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
public class DnsRequestAction implements WsAction<MapData> {
    @Autowired
    private DnsService dnsService;
    @Autowired
    private ConvertService convertService;

    @Override
    public void doAction(Client webSocket, WsMessage<MapData> msg) throws IOException {
        ComponentContext context = (ComponentContext) webSocket.getContext();
        if (context == null) {
            webSocket.close();
            return;
        }
        List<DnsEntity> dnsList = this.dnsService.listLocalNetworkDns(context.getNetworkId());
        List<DnsModel> dnsModels = dnsList.stream().map(convertService::initDnsModel).collect(Collectors.toList());
        NotifyData<List<DnsModel>> sendMsg = NotifyData.<List<DnsModel>>builder().type(Constant.NotifyType.COMPONENT_UPDATE_DNS).data(dnsModels).build();
        WsMessage<NotifyData<List<DnsModel>>> wsMessage = WsMessage.<NotifyData<List<DnsModel>>>builder().command(Constant.SocketCommand.COMPONENT_NOTIFY).data(sendMsg).build();
        webSocket.sendJsonPacket(wsMessage);
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.COMPONENT_DNS_REQUEST;
    }
}
