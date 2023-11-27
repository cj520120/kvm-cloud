package cn.chenjun.cloud.management.websocket.action.impl;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import cn.chenjun.cloud.management.model.DnsModel;
import cn.chenjun.cloud.management.servcie.DnsService;
import cn.chenjun.cloud.management.util.SpringContextUtils;
import cn.chenjun.cloud.management.websocket.WsManager;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.client.WsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.List;

@Component
public class DnsRequestAction implements WsAction {
    @Autowired
    private WsManager wsManager;

    @Override
    public void doAction(Session session, WsRequest<?> msg) throws IOException {
        WsClient client = wsManager.getClient(session);
        NotifyData<List<DnsModel>> sendMsg = NotifyData.<List<DnsModel>>builder().type(Constant.NotifyType.COMPONENT_UPDATE_DNS).data(SpringContextUtils.getBean(DnsService.class).listLocalNetworkDns(client.getNetworkId())).build();
        WsMessage<NotifyData<List<DnsModel>>> wsMessage = WsMessage.<NotifyData<List<DnsModel>>>builder().command(Constant.SocketCommand.NOTIFY).data(sendMsg).build();
        client.send(wsMessage);
    }

    @Override
    public int getType() {
        return Constant.SocketCommand.DNS_REQUEST;
    }
}
