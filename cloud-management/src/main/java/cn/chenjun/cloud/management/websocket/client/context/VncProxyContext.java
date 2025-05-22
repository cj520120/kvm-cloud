package cn.chenjun.cloud.management.websocket.client.context;

import cn.chenjun.cloud.common.event.EventListener;
import lombok.SneakyThrows;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * @author chenjun
 */
public class VncProxyContext extends WebSocketClient implements WebsocketContext {
    public final EventListener<Void> onConnect = new EventListener<>();
    public final EventListener<ByteBuffer> onMessage = new EventListener<>();
    public final EventListener<Void> onClose = new EventListener<>();

    public VncProxyContext(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, new Draft_6455(), httpHeaders);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        this.onConnect.fire(this, null);
    }

    @Override
    public void onMessage(String s) {
    }

    @SneakyThrows
    @Override
    public void onMessage(ByteBuffer bytes) {
        this.onMessage.fire(this, bytes);
    }

    @SneakyThrows
    @Override
    public void onClose(int i, String s, boolean b) {
        this.onClose.fire(this, null);
        this.close();
    }

    @SneakyThrows
    @Override
    public void onError(Exception e) {
        this.onClose.fire(this, null);
        this.close();
    }
}
