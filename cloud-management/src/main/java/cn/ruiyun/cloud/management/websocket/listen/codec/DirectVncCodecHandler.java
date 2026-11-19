package cn.ruiyun.cloud.management.websocket.listen.codec;

import cn.ruiyun.cloud.common.socket.packet.WsMessage;
import cn.ruiyun.cloud.management.servcie.bean.MemGraphicsInfo;
import cn.ruiyun.cloud.management.websocket.listen.client.AgentVncSocket;
import cn.ruiyun.cloud.management.websocket.listen.client.Client;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 浏览器与 Agent 之间的 VNC 原始字节透传，仅合并 WebSocket 分片。
 */
@Slf4j
public class DirectVncCodecHandler extends BaseCodecHandler<WsMessage<byte[]>, ByteBuffer> {

    private final ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream();
    private final Client browserSocket;

    public DirectVncCodecHandler(Client browserSocket) {
        this.browserSocket = browserSocket;
    }

    @SneakyThrows
    @Override
    public void onMessage(ByteBuffer messagePart, boolean last) {
        byte[] part = new byte[messagePart.remaining()];
        messagePart.get(part);
        messageBuffer.write(part);
        if (!last) {
            return;
        }

        try {
            Object socket = browserSocket.getAttribute(AgentVncSocket.ATTRIBUTE_NAME);
            if (!(socket instanceof AgentVncSocket)) {
                throw new IllegalStateException("Agent VNC WebSocket未连接");
            }
            AgentVncSocket agentVncSocket = (AgentVncSocket) socket;
            agentVncSocket.sendVncData(messageBuffer.toByteArray());
        } catch (Exception error) {
            MemGraphicsInfo graphics = (MemGraphicsInfo) browserSocket.getAttribute("vnc");
            log.error("VNC原始数据转发到Agent失败,guestId={},guestName={},hostId={}",
                    graphics == null ? 0 : graphics.getGuestId(),
                    graphics == null ? "unknown" : graphics.getGuestName(),
                    graphics == null ? 0 : graphics.getHostId(), error);
            try {
                browserSocket.close();
            } catch (IOException ignored) {
            }
        } finally {
            messageBuffer.reset();
        }
    }


    @Override
    public void close() throws IOException {
        messageBuffer.close();
    }
}
