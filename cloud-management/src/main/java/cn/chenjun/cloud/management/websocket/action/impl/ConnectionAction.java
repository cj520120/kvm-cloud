package cn.chenjun.cloud.management.websocket.action.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.model.LoginUserModel;
import cn.chenjun.cloud.management.servcie.UserService;
import cn.chenjun.cloud.management.websocket.WsManager;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.client.WsClient;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;

@Component
public class ConnectionAction implements WsAction {
    @Autowired
    private WsManager wsManager;
    @Autowired
    private UserService userService;
    @Autowired
    private NetworkMapper networkMapper;

    @Override
    public void doAction(Session session, WsRequest<?> msg) throws IOException {
        if (msg.getData() instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) msg.getData();
            int networkId = NumberUtil.parseInt(params.getOrDefault("networkId", "0").toString());
            String nonce = params.getOrDefault("nonce", "").toString();
            String sign = params.getOrDefault("sign", "").toString();
            NetworkEntity network =this.networkMapper.selectById(networkId);
            if (DigestUtil.md5Hex(network.getSecret() + ":" + networkId + ":" + nonce).equals(sign)) {
                WsClient wsClient = wsManager.registerComponentClient(session, networkId);
                WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.LOGIN_SUCCESS).build();
                wsClient.send(wsMessage);
            } else {
                WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.LOGIN_TOKEN_ERROR).build();
                session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(wsMessage));
            }
        } else if (msg.getData() instanceof String) {
            String token = (String) msg.getData();
            try {
                ResultUtil<LoginUserModel> resultUtil = this.userService.getUserIdByToken(token);
                if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                    WsClient wsClient = wsManager.registerWebClient(session);
                    WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.LOGIN_SUCCESS).build();
                    wsClient.send(wsMessage);
                } else {
                    WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.LOGIN_TOKEN_ERROR).build();
                    session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(wsMessage));
                }
            } catch (Exception err) {
                WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.LOGIN_TOKEN_ERROR).build();
                session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(wsMessage));
            }
        }

    }

    @Override
    public int getType() {
        return Constant.SocketCommand.CLIENT_CONNECT;
    }
}
