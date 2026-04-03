package cn.chenjun.cloud.management.websocket.listen.codec;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.management.websocket.action.ActionDispatcher;
import cn.chenjun.cloud.management.websocket.listen.client.Client;

import javax.websocket.MessageHandler;

public abstract class BaseCodecHandler<T extends WsMessage, V> implements MessageHandler.Whole<V> {
    public final Client webSocket;

    protected BaseCodecHandler(Client webSocket) {
        this.webSocket = webSocket;
    }

    @Override
    public void onMessage(V message) {
        T msg = decode(message);
        ActionDispatcher.dispatch(webSocket, msg);
    }

    protected abstract T decode(V message);
}
