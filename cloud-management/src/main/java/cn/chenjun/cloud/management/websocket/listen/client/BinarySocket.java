package cn.chenjun.cloud.management.websocket.listen.client;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.nio.ByteBuffer;


@Slf4j
public class BinarySocket extends BaseClient {
    protected final Session session;

    public BinarySocket(Session session, int socketType) {
        super(socketType);
        this.session = session;
    }


    @SneakyThrows
    @Override
    public <T> void sendJsonPacket(WsMessage<T> data) {
        throw new CodeException(ErrorCode.SERVER_ERROR, "不支持发送json数据包");
    }

    @Override
    protected void release() throws Exception {
        if (this.session != null && this.session.isOpen()) {
            this.session.close();
        }
    }

    @SneakyThrows
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
        session.getBasicRemote().sendBinary(buffer);
    }

    @Override
    public void sendCommand(int command) {
        this.sendBinaryPacket(command, null);
    }
}
