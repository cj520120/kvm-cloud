package cn.ruiyun.cloud.management.websocket.listen.server;

import cn.ruiyun.cloud.common.socket.packet.WsMessage;
import cn.ruiyun.cloud.management.websocket.common.SocketType;
import cn.ruiyun.cloud.management.websocket.listen.client.BinarySocket;
import cn.ruiyun.cloud.management.websocket.listen.client.Client;
import cn.ruiyun.cloud.management.websocket.listen.codec.ByteCodecHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.nio.ByteBuffer;

/**
 * @author chenjun
 */
@Slf4j
@Component
@ServerEndpoint(value = "/api/node/ws")
public class NodeListen extends AbstractWsService<WsMessage<byte[]>, ByteBuffer> {
    public NodeListen() {
        super(ByteCodecHandler::new);
    }


    @Override
    protected Client createWebSocket(Session session) {
        return new BinarySocket(session, SocketType.NODE_SOCKET);
    }

    @Override
    protected int getSocketType() {
        return SocketType.NODE_SOCKET;
    }

    @Override
    protected int getTimeoutSeconds() {
        return 30;
    }
}
