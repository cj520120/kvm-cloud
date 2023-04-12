package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.NotifyInfo;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
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

    public synchronized static void sendNotify(NotifyInfo message) {
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
        SESSIONS.add(this);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        SESSIONS.remove(this);
    }

    @OnMessage
    public void onMessage(byte[] messages, Session session) {

    }

    @OnClose
    public void onClose() {
        SESSIONS.remove(this);
    }
}
