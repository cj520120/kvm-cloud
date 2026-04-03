package cn.chenjun.cloud.agent.ws.client;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.event.EventListener;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.common.util.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class WsClient extends WebSocketClient {
    public final EventListener<Void> onConnect = new EventListener<>();
    public final EventListener<WsMessage<byte[]>> onMessage = new EventListener<>();
    public final EventListener<Void> onClose = new EventListener<>();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private boolean auth = false;

    public WsClient(URI serverUri) {
        super(serverUri, new Draft_6455());
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        this.onConnect.fire(this, null);
    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onMessage(ByteBuffer buffer) {
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
            this.onMessage.fire(this, msg);
        } catch (Exception e) {
            log.error("解析数据出错", e);
            this.close();
            throw new CodeException(ErrorCode.SERVER_ERROR, "解析数据出错");
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("连接断开，code:{},reason:{},remote={}", code, reason, remote);
        this.onClose.fire(this, null);
    }

    @Override
    public void onError(Exception ex) {
        log.info("连接出错", ex);
        this.close();
    }

    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            FunctionUtils.ignoreRun(super::close);
            FunctionUtils.ignoreRun(() -> this.onClose.fire(this, null));
            this.onClose.clear();
        }
    }

    public void sendCommand(int command) {
        this.sendBuffer(command, null);
    }

    public void sendJson(int command, Object data) {
        this.sendBuffer(command, GsonBuilderUtil.create().toJson(data).getBytes(StandardCharsets.UTF_8));
    }

    public synchronized void sendBuffer(int command, byte[] data) {
        if (this.isClosed.get()) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "连接已关闭");
        }
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
        try {
            this.send(buffer);
        } catch (Exception e) {
            log.error("发送数据失败,将关闭连接....", e);
            this.close();
        }
    }

    public void login() {
        this.auth = true;
    }

    public boolean isLogin() {
        return auth && this.isOpen();
    }
}
