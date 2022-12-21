package cn.roamblue.cloud.management.servcie;

import cn.roamblue.cloud.common.bean.NotifyInfo;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint(value = "/api/ws/")
public class WebSocketServerOne {
    private static final CopyOnWriteArraySet<WebSocketServerOne> SESSIONS = new CopyOnWriteArraySet<>();
    private Session session;

    public synchronized static void sendNotify(NotifyInfo message) {
        String msg = GsonBuilderUtil.create().toJson(message);
        for (WebSocketServerOne client : SESSIONS) {
            try {
                client.session.getBasicRemote().sendText(msg);
            } catch (Exception e) {
            }
        }
    }

    @SneakyThrows
    @OnOpen
    public void onConnect(Session session) {
        this.session = session;
        SESSIONS.add(this);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        SESSIONS.add(this);
    }

    @OnMessage
    public void onVncMessage(byte[] messages, Session session) {

    }

    @OnClose
    public void onClose() {
        SESSIONS.add(this);
    }
}
