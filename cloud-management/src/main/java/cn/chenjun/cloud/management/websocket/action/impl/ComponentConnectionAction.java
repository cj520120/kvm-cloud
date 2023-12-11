package cn.chenjun.cloud.management.websocket.action.impl;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.client.WsClient;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;

/**
 * @author chenjun
 */
@Component
public class ComponentConnectionAction implements WsAction {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private NetworkMapper networkMapper;

    @Override
    public void doAction(Session session, WsRequest msg) throws IOException {
        Map<String, Object> params = msg.getData();
            int networkId = NumberUtil.parseInt(params.getOrDefault("networkId", "0").toString());
            String nonce = params.getOrDefault("nonce", "").toString();
            String sign = params.getOrDefault("sign", "").toString();
            NetworkEntity network =this.networkMapper.selectById(networkId);
            if (DigestUtil.md5Hex(network.getSecret() + ":" + networkId + ":" + nonce).equals(sign)) {
                WsClient wsClient = wsSessionManager.registerComponentClient(session, networkId);
                WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.COMPONENT_CONNECT_SUCCESS).build();
                wsClient.send(wsMessage);
            } else {
                WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.COMPONENT_CONNECT_FAIL).build();
                session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(wsMessage));
            }


    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.COMPONENT_CONNECT;
    }
}
