package cn.ruiyun.cloud.management.websocket.listen.client;

import cn.ruiyun.cloud.common.util.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 将管理端收到的 noVNC 原始数据直接转发给宿主机 Agent。
 */
@Slf4j
public class AgentVncSocket extends WebSocketClient {
    public static final String ATTRIBUTE_NAME = AgentVncSocket.class.getName();

    private final VncSocket browserSocket;
    private final String connectionId;
    private final long guestId;
    private final String guestName;
    private final long hostId;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public AgentVncSocket(URI serverUri, Map<String, String> headers, VncSocket browserSocket, String connectionId,
                          long guestId, String guestName, long hostId) {
        super(serverUri, new Draft_6455(), headers);
        this.browserSocket = browserSocket;
        this.connectionId = connectionId;
        this.guestId = guestId;
        this.guestName = guestName;
        this.hostId = hostId;
    }

    public void sendVncData(byte[] data) {
        if (!isOpen()) {
            throw new IllegalStateException("Agent VNC WebSocket未连接");
        }
        send(data);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        log.info("Agent VNC直连成功,guestId={},guestName={},hostId={},sessionId={},target={}",
                guestId, guestName, hostId, connectionId, getURI());
    }

    @Override
    public void onMessage(String message) {
        log.warn("忽略Agent VNC文本消息,guestId={},guestName={},hostId={},sessionId={}",
                guestId, guestName, hostId, connectionId);
    }

    @Override
    public void onMessage(ByteBuffer buffer) {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        browserSocket.sendVncData(data);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("Agent VNC直连关闭,guestId={},guestName={},hostId={},sessionId={},code={},reason={},remote={}",
                guestId, guestName, hostId, connectionId, code, reason, remote);
        close();
    }

    @Override
    public void onError(Exception error) {
        log.error("Agent VNC直连异常,guestId={},guestName={},hostId={},sessionId={},target={}",
                guestId, guestName, hostId, connectionId, getURI(), error);
        close();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            FunctionUtils.ignoreRun(super::close);
            FunctionUtils.ignoreRun(browserSocket::close);
        }
    }
}
