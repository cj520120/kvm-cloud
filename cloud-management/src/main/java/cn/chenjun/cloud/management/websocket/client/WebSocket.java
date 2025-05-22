package cn.chenjun.cloud.management.websocket.client;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.event.EventListener;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.websocket.client.context.WebsocketContext;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

import javax.websocket.Session;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@Builder
public class WebSocket {
    public final EventListener<Void> onClose = new EventListener<>();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private Session session;
    private WebsocketContext context;

    @SneakyThrows
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            try {
                session.close();
                this.onClose.fire(this, null);
            } catch (Exception ignored) {

            }
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
