package cn.chenjun.cloud.management.websocket.action.impl;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.DnsModel;
import cn.chenjun.cloud.management.servcie.DnsService;
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
public class DnsRequestAction implements WsAction {
    @Autowired
    private DnsService dnsService;

    @Override
    public void doAction(Session session, WsRequest msg) throws IOException {
        WsClient<ComponentWsOwner> client = WsSessionManager.getClient(session);
        if (client == null) {
            return;
        }
        NotifyData<List<DnsModel>> sendMsg = NotifyData.<List<DnsModel>>builder().type(Constant.NotifyType.COMPONENT_UPDATE_DNS).data(this.dnsService.listLocalNetworkDns(client.getOwner().getNetworkId())).build();
        WsMessage<NotifyData<List<DnsModel>>> wsMessage = WsMessage.<NotifyData<List<DnsModel>>>builder().command(Constant.SocketCommand.COMPONENT_NOTIFY).data(sendMsg).build();
        session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(wsMessage));
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.COMPONENT_DNS_REQUEST;
    }
}
