package cn.chenjun.cloud.management.websocket.listen.client;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.event.EventHandler;
import cn.chenjun.cloud.common.event.EventListener;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.common.util.FunctionUtils;
import cn.chenjun.cloud.management.websocket.action.ActionDispatcher;
import cn.chenjun.cloud.management.websocket.common.SocketType;
import cn.chenjun.cloud.management.websocket.listen.context.ConnectContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class NodeSocket extends WebSocketClient implements ConnectContext, Client {
    public final EventListener<ConnectContext> onClose = new EventListener<>();
    public final EventListener<ConnectContext> onLogin = new EventListener<>();
    public final EventListener<Void> onConnect = new EventListener<>();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final String sessionId = UUID.randomUUID().toString();
    private final List<ByteBuffer> cacheBuffer = new ArrayList<>();
    protected ConnectContext context;
    private long lastActiveTime;
    private long lastPingTime;
    private boolean isConnecting = false;

    public NodeSocket(URI serverUri) {
        super(serverUri, new Draft_6455(), new HashMap<>());
        this.lastPingTime = System.currentTimeMillis();
        this.lastActiveTime = System.currentTimeMillis();
    }


    @Override
    public Object putAttribute(String key, Object value) {
        return this.attributes.put(key, value);
    }

    @Override
    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    @Override
    public int getType() {
        return SocketType.NODE_SOCKET;
    }

    @Override
    public String getSessionId() {
        return this.sessionId;
    }

    public ConnectContext getContext() {
        return this.context;
    }

    public void setContext(ConnectContext context) {
        this.context = context;
    }

    @Override
    public void registerOnClose(EventHandler<ConnectContext> onCloseHandler) {
        this.onClose.addEvent(onCloseHandler);
    }

    @Override
    public void unregisterOnClose(EventHandler<ConnectContext> onCloseHandler) {
        this.onClose.removeEvent(onCloseHandler);
    }

    @Override
    public void registerOnLogin(EventHandler<ConnectContext> onLoginHandler) {
        this.onLogin.addEvent(onLoginHandler);

    }

    @Override
    public void unregisterOnLogin(EventHandler<ConnectContext> onLoginHandler) {
        this.onLogin.removeEvent(onLoginHandler);
    }

    public void registerOnConnect(EventHandler<Void> onConnectHandler) {
        this.onConnect.addEvent(onConnectHandler);
    }

    public void unregisterOnConnect(EventHandler<Void> onConnectHandler) {
        this.onConnect.removeEvent(onConnectHandler);
    }

    @Override
    public long getLastActiveTime() {
        return this.lastActiveTime;
    }

    @Override
    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    @Override
    public long getLastPingTime() {
        return lastPingTime;
    }

    @Override
    public void setLastPingTime(long lastPingTime) {
        this.lastPingTime = lastPingTime;
    }

    @Override
    public void login(ConnectContext context) {
        this.context = context;
        try {
            this.onLogin.fire(this, this.context);
        } catch (Exception err) {
            log.error("websocket login error", err);
        }
    }

    @Override
    public void sendCommand(int command) {
        this.sendBinaryPacket(command, null);
    }

    private synchronized void sendCacheBuffer() {
        for (ByteBuffer buffer : cacheBuffer) {
            this.send(buffer);
        }
        cacheBuffer.clear();

    }

    @SneakyThrows
    @Override
    public <T> void sendJsonPacket(WsMessage<T> data) {
        throw new CodeException(ErrorCode.SERVER_ERROR, "不支持发送json数据包");
    }

    @Override
    public synchronized void sendBinaryPacket(int command, byte[] data) {
        int length = 8;
        if (data != null) {
            length += data.length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.putInt(command);
        buffer.putInt(data == null ? 0 : data.length);
        if (data != null) {
            buffer.put(data);
        }
        buffer.flip();
        if (!isConnecting) {
            cacheBuffer.add(buffer);
        } else {
            try {
                this.send(buffer);
            } catch (Exception err) {
                log.error("websocket send error", err);
                this.close();
            }
        }
    }

    @SneakyThrows
    @Override
    public void close() {
        FunctionUtils.ignoreRun(super::close);
        if (isClosed.compareAndSet(false, true)) {
            FunctionUtils.ignoreRun(() -> this.onClose.fire(this, this.context));
            this.onClose.clear();
            this.onLogin.clear();
            this.onConnect.clear();
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        this.isConnecting = true;
        this.sendCacheBuffer();
        this.onConnect.fire(this, null);
    }

    @Override
    public void onMessage(String message) {

    }

    @SneakyThrows
    @Override
    public void onMessage(ByteBuffer buffer) {
        int command = buffer.getInt();
        int dataLength = buffer.getInt();
        byte[] dataBuffer = new byte[dataLength];
        if (dataLength > 0) {
            buffer.get(dataBuffer, 0, dataLength);
        }
        WsMessage<byte[]> msg = new WsMessage<>();
        msg.setCommand(command);
        msg.setData(dataBuffer);
        ActionDispatcher.dispatch(this, msg);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        this.close();
    }

    @Override
    public void onError(Exception ex) {
        this.close();
    }
}
