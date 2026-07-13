package cn.chenjun.cloud.management.websocket.listen.client;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.nio.ByteBuffer;


@Slf4j
public class BinarySocket extends BaseClient {
    protected final Session session;
    final int CHUNK_SIZE = 32768;

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
        int headerLen = 8;
        int totalLen = headerLen + (data == null ? 0 : data.length);
        ByteBuffer fullBuf = ByteBuffer.allocate(totalLen);
        fullBuf.putInt(command);
        fullBuf.putInt(data == null ? 0 : data.length);
        if (data != null) {
            fullBuf.put(data);
        }
        fullBuf.rewind();

        RemoteEndpoint.Basic basic = session.getBasicRemote();

        while (fullBuf.hasRemaining()) {
            int readLen = Math.min(CHUNK_SIZE, fullBuf.remaining());
            // 零拷贝切片，不新建数组
            ByteBuffer chunk = fullBuf.slice();
            chunk.limit(readLen);
            fullBuf.position(fullBuf.position() + readLen);
            boolean isLast = !fullBuf.hasRemaining();
            basic.sendBinary(chunk, isLast);
        }
    }

    @Override
    public void sendCommand(int command) {
        this.sendBinaryPacket(command, null);
    }
}
