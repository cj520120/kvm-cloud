package cn.chenjun.cloud.management.websocket.listen.codec;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.management.websocket.action.ActionDispatcher;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;

@Slf4j
public class JsonCodecHandler<T extends WsMessage> extends BaseCodecHandler<T, String> {
    private final Type clazz;
    private final Client webSocket;
    private final StringBuilder messageBuffer = new StringBuilder();

    public JsonCodecHandler(Client sock, Type clazz) {
        this.webSocket = sock;
        this.clazz = clazz;
    }


    @Override
    public void close() throws IOException {

    }

    @Override
    public void onMessage(String messagePart, boolean last) {
        messageBuffer.append(messagePart);
        if (last) {
            String message = messageBuffer.toString();
            messageBuffer.setLength(0);
            T msg = GsonBuilderUtil.create().fromJson(message, clazz);
            ActionDispatcher.dispatch(webSocket, msg);
        }
    }
}
