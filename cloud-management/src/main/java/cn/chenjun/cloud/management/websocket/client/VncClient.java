package cn.chenjun.cloud.management.websocket.client;

import lombok.SneakyThrows;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import javax.websocket.Session;
import java.net.URI;
import java.nio.ByteBuffer;

/**
 * @author chenjun
 */
public class VncClient extends WebSocketClient {

    private final Session session;
    private final WsCallback callback;

    public VncClient(Session session, URI serverUri, WsCallback callback) {
        super(serverUri, new Draft_6455());
        this.session = session;
        this.callback = callback;

    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        this.callback.onConnect();
    }

    @Override
    public void onMessage(String s) {
    }

    @SneakyThrows
    @Override
    public void onMessage(ByteBuffer bytes) {
        session.getBasicRemote().sendBinary(bytes);
    }

    @SneakyThrows
    @Override
    public void onClose(int i, String s, boolean b) {
        this.session.close();
        this.close();
    }

    @SneakyThrows
    @Override
    public void onError(Exception e) {
        this.session.close();
        this.close();
    }

    @FunctionalInterface
    public interface WsCallback {
        void onConnect();
    }
}
