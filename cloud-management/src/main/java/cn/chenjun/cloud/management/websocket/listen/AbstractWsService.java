package cn.chenjun.cloud.management.websocket.listen;

import cn.chenjun.cloud.management.websocket.client.WebSocket;
import lombok.SneakyThrows;

import javax.websocket.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenjun
 */
public abstract class AbstractWsService<T> {

    private static final ConcurrentHashMap<String, WebSocket> WEBSOCKET_CACHE = new ConcurrentHashMap<>();

    @SneakyThrows
    @OnOpen
    public final void onOpen(Session session) {
        WebSocket webSocket = WebSocket.builder().session(session).build();
        session.addMessageHandler(createMessageHandler(webSocket));
        WEBSOCKET_CACHE.put(session.getId(), webSocket);
        this.onConnection(webSocket);
    }

    @OnError
    public final void onError(Session session, Throwable error) {
        this.onClose(session);
    }


    @SneakyThrows
    @OnClose
    public final void onClose(Session session) {
        WebSocket webSocket = WEBSOCKET_CACHE.remove(session.getId());
        if (webSocket != null) {
            webSocket.close();
        }
    }

    protected abstract MessageHandler.Whole<T> createMessageHandler(WebSocket webSocket);

    protected void onConnection(WebSocket webSocket) {
    }

}
