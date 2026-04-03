package cn.chenjun.cloud.management.websocket.listen.client;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;


@Slf4j
public class JsonSocket extends BaseClient {
    protected final Session session;

    public JsonSocket(Session session, int socketType) {
        super(socketType);
        this.session = session;
    }


    @SneakyThrows
    @Override
    public synchronized <T> void sendJsonPacket(WsMessage<T> data) {
        session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(data));
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
