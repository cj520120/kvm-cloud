package cn.chenjun.cloud.management.websocket.action.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.model.LoginUserModel;
import cn.chenjun.cloud.management.servcie.UserService;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.client.WsClient;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;

/**
 * @author chenjun
 */
@Component
public class WebConnectionAction implements WsAction {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private UserService userService;
    @Autowired
    private NetworkMapper networkMapper;

    @Override
    public void doAction(Session session, WsRequest msg) throws IOException {
        try {
            String token = (String) msg.getData().get("token");
            ResultUtil<LoginUserModel> resultUtil = this.userService.getUserIdByToken(token);
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                WsClient wsClient = wsSessionManager.registerWebClient(session);
                WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.WEB_LOGIN_SUCCESS).build();
                wsClient.send(wsMessage);
            } else {
                WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.WEB_LOGIN_TOKEN_ERROR).build();
                session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(wsMessage));
            }
        } catch (Exception err) {
            WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.WEB_LOGIN_TOKEN_ERROR).build();
            session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(wsMessage));
        }

    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.WEB_LOGIN;
    }
}
