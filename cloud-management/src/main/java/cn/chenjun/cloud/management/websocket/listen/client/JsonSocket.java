package cn.chenjun.cloud.management.websocket.listen.client;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;


@Slf4j
public class JsonSocket extends BaseClient {
    private static final int CHUNK_SIZE = 32768;
    protected final Session session;

    public JsonSocket(Session session, int socketType) {
        super(socketType);
        this.session = session;
    }


    @SneakyThrows
    @Override
    public synchronized <T> void sendJsonPacket(WsMessage<T> data) {
        String fullText = GsonBuilderUtil.create().toJson(data);
        RemoteEndpoint.Basic basic = session.getBasicRemote();
        int totalLength = fullText.length();
        int offset = 0;
        while (offset < totalLength) {
            // 截取一段分片
            int end = Math.min(offset + CHUNK_SIZE, totalLength);
            String chunk = fullText.substring(offset, end);
            // 是否最后一帧
            boolean isLast = end == totalLength;
            // 发送分片帧
            basic.sendText(chunk, isLast);
            // 偏移前进
            offset = end;
        }
    }

    @Override
    protected void release() throws Exception {
        if (this.session != null && this.session.isOpen()) {
            this.session.close();
        }
    }

    @SneakyThrows
    @Override
    public void sendBinaryPacket(int command, byte[] data) {
        throw new RuntimeException("不支持发送二进制数据包");
    }

    @Override
    public void sendCommand(int command) {
        this.sendJsonPacket(WsMessage.<Void>builder().command(command).build());
    }
}
