package cn.chenjun.cloud.management.websocket.action.impl;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.client.WebSocket;
import cn.chenjun.cloud.management.websocket.client.context.ComponentContext;
import cn.chenjun.cloud.management.websocket.manager.ComponentClientManager;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author chenjun
 */
@Component
public class ComponentConnectionAction implements WsAction {

    @Autowired
    private NetworkMapper networkMapper;

    @Override
    public void doAction(WebSocket webSocket, WsRequest msg) throws IOException {
        Map<String, Object> params = msg.getData();
        int networkId = NumberUtil.parseInt(params.getOrDefault("networkId", "0").toString());
        int componentId = NumberUtil.parseInt(params.getOrDefault("componentId", "0").toString());
        String nonce = params.getOrDefault("nonce", "").toString();
        String sign = params.getOrDefault("sign", "").toString();
        NetworkEntity network = this.networkMapper.selectById(networkId);
        if (DigestUtil.md5Hex(network.getSecret() + ":" + networkId + ":" + componentId + ":" + nonce).equals(sign)) {
            webSocket.setContext(ComponentContext.builder().networkId(networkId).componentId(componentId).build());
            ComponentClientManager.addComponentClient(componentId, webSocket);
            WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.COMPONENT_CONNECT_SUCCESS).build();
            webSocket.send(wsMessage);
        } else {
            WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.COMPONENT_CONNECT_FAIL).build();
            webSocket.send(wsMessage);
        }

    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.COMPONENT_CONNECT;
    }
}
