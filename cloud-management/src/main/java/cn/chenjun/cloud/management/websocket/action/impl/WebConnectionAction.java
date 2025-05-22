package cn.chenjun.cloud.management.websocket.action.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.model.LoginUserModel;
import cn.chenjun.cloud.management.servcie.UserService;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.client.WebSocket;
import cn.chenjun.cloud.management.websocket.client.context.UserContext;
import cn.chenjun.cloud.management.websocket.manager.WebClientManager;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author chenjun
 */
@Component
public class WebConnectionAction implements WsAction {
    @Autowired
    private UserService userService;
    @Autowired
    private NetworkMapper networkMapper;

    @Override
    public void doAction(WebSocket webSocket, WsRequest msg) throws IOException {
        try {
            String token = (String) msg.getData().get("token");
            ResultUtil<LoginUserModel> resultUtil = this.userService.getUserIdByToken(token);
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                LoginUserModel user = resultUtil.getData();
                webSocket.setContext(UserContext.builder().userId(user.getUserId()).build());
                WebClientManager.addClient(user.getUserId(), webSocket);
                WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.WEB_LOGIN_SUCCESS).build();
                webSocket.send(wsMessage);
            } else {
                WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.WEB_LOGIN_TOKEN_ERROR).build();
                webSocket.send(wsMessage);
            }
        } catch (Exception err) {
            WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.WEB_LOGIN_TOKEN_ERROR).build();
            webSocket.send(wsMessage);
        }

    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.WEB_LOGIN;
    }
}
