package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.SocketMessage;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.model.LoginUserModel;
import cn.chenjun.cloud.management.util.SpringUtil;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author chenjun
 */
@Slf4j
@Component
@ServerEndpoint(value = "/api/ws/")
@EqualsAndHashCode
public class WebSocketServerOne {
    private static final CopyOnWriteArraySet<WebSocketServerOne> SESSIONS = new CopyOnWriteArraySet<>();
    private Session session;
    private LoginUserModel loginInfo;

    public synchronized static void sendNotify(SocketMessage message) {
        String msg = GsonBuilderUtil.create().toJson(message);
        for (WebSocketServerOne client : SESSIONS) {
            try {
                client.session.getBasicRemote().sendText(msg);
            } catch (Exception e) {
                log.info("写入通知消息出错。",e);
            }
        }
    }

    @SneakyThrows
    @OnOpen
    public void onConnect(Session session) {
        this.session = session;
    }

    @OnError
    public void onError(Session session, Throwable error) {
        SESSIONS.remove(this);
    }

    @SneakyThrows
    @OnMessage
    public void onMessage(String jsonMsg) {
        SocketMessage msg = GsonBuilderUtil.create().fromJson(jsonMsg, SocketMessage.class);
        if (msg.getType() == Constant.SocketCommand.CLIENT_CONNECT) {
            String token = msg.getData();
            try {
                ResultUtil<LoginUserModel> resultUtil = SpringUtil.getBean(UserService.class).getUserIdByToken(token);
                if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                    this.loginInfo = resultUtil.getData();
                    SESSIONS.add(this);
                    session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(SocketMessage.builder().id(0).type(Constant.SocketCommand.LOGIN_SUCCESS).build()));
                } else {
                    throw new CodeException(ErrorCode.NO_LOGIN_ERROR);
                }
            } catch (Exception err) {
                this.session.close();
            }
        }
    }

    @OnClose
    public void onClose() {
        SESSIONS.remove(this);
    }


}
