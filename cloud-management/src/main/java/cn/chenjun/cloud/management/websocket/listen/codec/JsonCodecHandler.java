package cn.chenjun.cloud.management.websocket.listen.codec;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

@Slf4j
public class JsonCodecHandler<T extends WsMessage> extends BaseCodecHandler<T, String> {
    private final Type clazz;

    public JsonCodecHandler(Client sock, Type clazz) {
        super(sock);
        this.clazz = clazz;
    }

    @Override
    protected T decode(String message) {
        return GsonBuilderUtil.create().fromJson(message, clazz);
    }
}
