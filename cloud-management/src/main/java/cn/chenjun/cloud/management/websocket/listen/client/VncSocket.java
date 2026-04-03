package cn.chenjun.cloud.management.websocket.listen.client;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.host.VncData;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.websocket.common.SocketType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.nio.ByteBuffer;

@Slf4j
public class VncSocket extends BaseClient {
    private final Session session;

    public VncSocket(Session session) {
        super(SocketType.VNC_SOCKET);
        this.session = session;
    }

    @Override
    protected void release() throws Exception {
        if (this.session != null && this.session.isOpen()) {
            this.session.close();
        }
    }


    @SneakyThrows
    @Override
    public <T> void sendJsonPacket(WsMessage<T> data) {
        throw new CodeException(ErrorCode.SERVER_ERROR, "不支持发送json数据");
    }

    @Override
    public synchronized void sendBinaryPacket(int command, byte[] data) {
        try {
            switch (command) {
                case Constant.SocketCommand.VNC_DATA:
                    VncData vncData = VncData.fromBytes(data);
                    session.getBasicRemote().sendBinary(ByteBuffer.wrap(vncData.getData()));
                    break;
                case Constant.SocketCommand.VNC_DISCONNECT:
                    this.close();
                    break;
                case Constant.SocketCommand.PING:
                    this.setLastActiveTime(System.currentTimeMillis());
                    break;
                default:
                    throw new CodeException(ErrorCode.SERVER_ERROR, "无效的处理类型");
            }
        } catch (Exception e) {
            log.error("sendBinaryPacket error", e);
            close();
        }
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void sendCommand(int command) {
        this.sendBinaryPacket(command, new byte[0]);
    }
}
