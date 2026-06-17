package cn.chenjun.cloud.agent.ws.client;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.event.EventListener;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.common.util.FunctionUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@ClientEndpoint
public class WsClient {

    // 对外事件，与原 WsClient 完全一致
    public final EventListener<Void> onConnect = new EventListener<>();
    public final EventListener<WsMessage<byte[]>> onMessage = new EventListener<>();
    public final EventListener<Void> onClose = new EventListener<>();
    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final URI serverUri;
    private volatile Session session;
    private boolean auth = false;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final AtomicLong lastSendTime = new AtomicLong(System.currentTimeMillis());

    public WsClient(URI serverUri) {
        this.serverUri = serverUri;
    }

    // 建立连接
    public void connect() throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.setDefaultMaxBinaryMessageBufferSize(65535);
        container.setDefaultMaxTextMessageBufferSize(65535);
        container.setDefaultMaxSessionIdleTimeout(60 * 60 * 1000L);

        // ⭐ 将 this 作为 ClientEndpoint，并将 URI 配置传入
        this.session = container.connectToServer(this, serverUri);
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        lastSendTime.set(System.currentTimeMillis());
        log.info("WebSocket 连接成功");
        onConnect.fire(this, null);
    }

    @SneakyThrows
    @OnMessage
    public void onPartialMessage(ByteBuffer partialBuffer, boolean last, Session session) {
        byte[] chunk = new byte[partialBuffer.remaining()];
        partialBuffer.get(chunk);
        byteArrayOutputStream.write(chunk, 0, chunk.length);
        if (last) {
            byte[] completeData = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.reset();
            ByteBuffer completeBuffer = ByteBuffer.wrap(completeData);
            processMessage(completeBuffer, session);
        }
    }

    public void processMessage(ByteBuffer buffer, Session session) {
        try {
            int command = buffer.getInt();
            int dataLength = buffer.getInt();
            byte[] dataBuffer = new byte[dataLength];
            if (dataLength > 0) {
                buffer.get(dataBuffer, 0, dataLength);
            }
            WsMessage<byte[]> msg = new WsMessage<>();
            msg.setCommand(command);
            msg.setData(dataBuffer);
            onMessage.fire(this, msg);
        } catch (Exception e) {
            log.error("解析二进制消息出错", e);
            close();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 处理文本消息，根据需要实现
        log.debug("收到文本消息: {}", message);
    }

    @OnClose
    public void onClose(CloseReason reason) {
        log.info("连接断开，原因: {}", reason.getReasonPhrase());
        isClosed.set(true);
        onClose.fire(this, null);

    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 错误", error);
        close();
    }

    // 发送命令
    public void sendCommand(int command) {
        sendBuffer(command, null);
    }

    public void sendJson(int command, Object data) {
        sendBuffer(command, GsonBuilderUtil.create().toJson(data).getBytes(StandardCharsets.UTF_8));
    }

    public synchronized void sendBuffer(int command, byte[] data) {
        if (isClosed.get() || session == null || !session.isOpen()) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "连接已关闭");
        }
        int headerLen = 8;
        int totalLen = headerLen + (data != null ? data.length : 0);
        ByteBuffer fullBuf = ByteBuffer.allocate(totalLen);
        fullBuf.putInt(command);
        fullBuf.putInt(data == null ? 0 : data.length);
        if (data != null) {
            fullBuf.put(data);
        }
        fullBuf.rewind();

        final int CHUNK_SIZE = 32768;
        RemoteEndpoint.Basic basic = session.getBasicRemote();
        try {
            while (fullBuf.hasRemaining()) {
                int readLen = Math.min(CHUNK_SIZE, fullBuf.remaining());
                // 零拷贝切片，不复制字节
                ByteBuffer chunk = fullBuf.slice();
                chunk.limit(readLen);
                fullBuf.position(fullBuf.position() + readLen);
                boolean isLast = !fullBuf.hasRemaining();

                basic.sendBinary(chunk, isLast);
            }
            lastSendTime.set(System.currentTimeMillis());
        } catch (IOException e) {
            log.error("发送数据失败，将关闭连接...", e);
            close();
            throw new CodeException(ErrorCode.SERVER_ERROR, "发送失败", e);
        }
    }

    public void login() {
        this.auth = true;
    }

    public boolean isLogin() {
        return auth && session != null && session.isOpen();
    }

    public long getLastSendTime() {
        return lastSendTime.get();
    }

    @SneakyThrows
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            FunctionUtils.ignoreRun(() -> {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            });
            onClose.fire(this, null);
            onClose.clear();
            byteArrayOutputStream.close();
        }
    }
}