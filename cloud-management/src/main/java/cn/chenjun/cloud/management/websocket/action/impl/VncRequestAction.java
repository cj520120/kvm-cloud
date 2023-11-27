package cn.chenjun.cloud.management.websocket.action.impl;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.VncModel;
import cn.chenjun.cloud.management.servcie.VncService;
import cn.chenjun.cloud.management.util.SpringContextUtils;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.client.WsClient;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.List;

/**
 * @author chenjun
 */
@Component
public class VncRequestAction implements WsAction {
    @Autowired
    private WsSessionManager wsSessionManager;

    @Override
    public void doAction(Session session, WsRequest msg) throws IOException {
        WsClient client = wsSessionManager.getClient(session);
        NotifyData<List<VncModel>> sendMsg = NotifyData.<List<VncModel>>builder().type(Constant.NotifyType.COMPONENT_UPDATE_VNC).data(SpringContextUtils.getBean(VncService.class).listVncByNetworkId(client.getNetworkId())).build();
        WsMessage<NotifyData<List<VncModel>>> wsMessage = WsMessage.<NotifyData<List<VncModel>>>builder().command(Constant.SocketCommand.COMPONENT_NOTIFY).data(sendMsg).build();
        client.send(wsMessage);
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.COMPONENT_VNC_REQUEST;
    }
}
