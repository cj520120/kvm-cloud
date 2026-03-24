package cn.chenjun.cloud.management.websocket.client;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.event.EventListener;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.websocket.client.context.WebsocketContext;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@Builder
@Slf4j
public class WebSocket {
    public final EventListener<WebsocketContext> onClose = new EventListener<>();
    public final EventListener<WebsocketContext> onLogin = new EventListener<>();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private Session session;
    private WebsocketContext context;
    private long lastActiveTime;

    @SneakyThrows
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            try {
                session.close();
            } catch (Exception ignored) {
            }
            try {
                this.onClose.fire(this, this.context);
            } catch (Exception ignored) {
            }
        }
    }

    public void login(WebsocketContext context) {
        this.context = context;
        try {
            this.onLogin.fire(this, this.context);
        } catch (Exception err) {
            log.error("websocket login error", err);
        }
    }
    @SneakyThrows
    public void send(ByteBuffer data) {
        session.getBasicRemote().sendBinary(data);
    }

    @SneakyThrows
    public <T> void send(WsMessage<T> data) {
        session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(data));
    }

}
