package cn.ruiyun.cloud.management.websocket.listen.client;


import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.socket.packet.WsMessage;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.management.servcie.bean.MemGraphicsInfo;
import cn.ruiyun.cloud.management.websocket.common.SocketType;
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
                case Constant.SocketCommand.PING:
                    session.getBasicRemote().sendPing(ByteBuffer.wrap(new byte[0]));
                    this.setLastActiveTime(System.currentTimeMillis());
                    break;
                default:
                    throw new CodeException(ErrorCode.SERVER_ERROR, "无效的处理类型");
            }
        } catch (Exception e) {
            MemGraphicsInfo graphics = graphics();
            log.error("发送VNC控制数据失败,guestId={},guestName={},hostId={}",
                    guestId(graphics), guestName(graphics), hostId(graphics), e);
            close();
        }
    }

    public synchronized void sendVncData(byte[] data) {
        try {
            session.getBasicRemote().sendBinary(ByteBuffer.wrap(data));
        } catch (Exception error) {
            MemGraphicsInfo graphics = graphics();
            log.error("发送VNC原始数据到浏览器失败,guestId={},guestName={},hostId={}",
                    guestId(graphics), guestName(graphics), hostId(graphics), error);
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

    private MemGraphicsInfo graphics() {
        Object value = getAttribute("vnc");
        return value instanceof MemGraphicsInfo ? (MemGraphicsInfo) value : null;
    }

    private long guestId(MemGraphicsInfo graphics) {
        return graphics == null ? 0 : graphics.getGuestId();
    }

    private String guestName(MemGraphicsInfo graphics) {
        return graphics == null ? "unknown" : graphics.getGuestName();
    }

    private long hostId(MemGraphicsInfo graphics) {
        return graphics == null ? 0 : graphics.getHostId();
    }
}
