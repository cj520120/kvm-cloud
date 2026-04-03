package cn.chenjun.cloud.management.websocket.action;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.management.websocket.listen.client.Client;

import java.io.IOException;

/**
 * @author chenjun
 */
public interface WsAction<T> {
    /**
     * ws 消息处理
     *
     * @param webSocket
     * @param msg
     * @throws IOException
     */
    void doAction(Client webSocket, WsMessage<T> msg) throws IOException;

    /**
     * 消息类型
     *
     * @return
     */
    int getCommand();
}
