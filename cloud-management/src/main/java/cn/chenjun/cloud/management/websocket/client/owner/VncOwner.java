package cn.chenjun.cloud.management.websocket.client.owner;

import cn.chenjun.cloud.common.event.EventListener;
import cn.chenjun.cloud.management.util.Constant;
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
public class VncOwner extends WebSocketClient implements WsOwner {
    public final EventListener<Void> onConnect = new EventListener<>();
    public final EventListener<ByteBuffer> onMessage = new EventListener<>();
    public final EventListener<Void> onClose = new EventListener<>();

    public VncOwner(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, new Draft_6455(), httpHeaders);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        this.onConnect.notify(this, null);
    }

    @Override
    public void onMessage(String s) {
    }

    @SneakyThrows
    @Override
    public void onMessage(ByteBuffer bytes) {
        this.onMessage.notify(this, bytes);
    }

    @SneakyThrows
    @Override
    public void onClose(int i, String s, boolean b) {
        this.onClose.notify(this, null);
        this.close();
    }

    @SneakyThrows
    @Override
    public void onError(Exception e) {
        this.onClose.notify(this, null);
        this.close();
    }


    @Override
    public short getType() {
        return Constant.WsClientType.VNC;
    }
}
