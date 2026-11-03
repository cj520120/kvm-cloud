package cn.ruiyun.cloud.management.websocket.action.impl.web;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.socket.packet.WsMessage;
import cn.ruiyun.cloud.common.socket.packet.data.base.MapData;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.management.model.LoginUserModel;
import cn.ruiyun.cloud.management.servcie.UserService;
import cn.ruiyun.cloud.management.websocket.action.WsAction;
import cn.ruiyun.cloud.management.websocket.listen.client.Client;
import cn.ruiyun.cloud.management.websocket.listen.context.UserContext;
import cn.ruiyun.cloud.management.websocket.manager.WebClientManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author chenjun
 */
@Component
public class WebConnectionAction implements WsAction<MapData> {
    @Autowired
    private UserService userService;

    @Override
    public void doAction(Client webSocket, WsMessage<MapData> msg) throws IOException {
        try {
            String token = (String) msg.getData().get("token");
            ResultUtil<LoginUserModel> resultUtil = this.userService.getUserIdByToken(token);
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                LoginUserModel user = resultUtil.getData();
                UserContext context = UserContext.builder().userId(user.getUserId()).build();
                webSocket.login(context);
                WebClientManager.addClient(user.getUserId(), webSocket);
                WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.WEB_LOGIN_SUCCESS).build();
                webSocket.sendJsonPacket(wsMessage);
            } else {
                WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.WEB_LOGIN_TOKEN_ERROR).build();
                webSocket.sendJsonPacket(wsMessage);
            }
        } catch (Exception err) {
            WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.WEB_LOGIN_TOKEN_ERROR).build();
            webSocket.sendJsonPacket(wsMessage);
        }

    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.WEB_LOGIN;
    }
}
