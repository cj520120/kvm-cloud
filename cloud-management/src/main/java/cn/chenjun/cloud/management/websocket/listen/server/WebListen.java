package cn.chenjun.cloud.management.websocket.listen.server;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.base.MapData;
import cn.chenjun.cloud.management.websocket.common.SocketType;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.listen.client.JsonSocket;
import cn.chenjun.cloud.management.websocket.listen.codec.JsonCodecHandler;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * @author chenjun
 */
@Slf4j
@Component
@ServerEndpoint(value = "/api/ws/")
public class WebListen extends AbstractWsService<String> {

    public WebListen() {
        super((client) -> new JsonCodecHandler<WsMessage<MapData>>(client, new TypeToken<WsMessage<MapData>>() {
        }.getType()));
    }


    @Override
    protected Client createWebSocket(Session session) {
        return new JsonSocket(session, SocketType.WEB_SOCKET);
    }

    @Override
    protected int getSocketType() {
        return SocketType.WEB_SOCKET;
    }

    @Override
    protected int getTimeoutSeconds() {
        return 30;
    }
}
