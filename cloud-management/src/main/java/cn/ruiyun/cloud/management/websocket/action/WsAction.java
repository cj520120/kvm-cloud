package cn.ruiyun.cloud.management.websocket.action;

import cn.ruiyun.cloud.common.socket.packet.WsMessage;
import cn.ruiyun.cloud.management.websocket.listen.client.Client;

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
