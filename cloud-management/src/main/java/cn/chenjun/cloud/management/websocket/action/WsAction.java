package cn.chenjun.cloud.management.websocket.action;

import cn.chenjun.cloud.management.websocket.client.WebSocket;
import cn.chenjun.cloud.management.websocket.message.WsRequest;

import java.io.IOException;

/**
 * @author chenjun
 */
public interface WsAction {
    /**
     * ws 消息处理
     *
     * @param webSocket
     * @param msg
     * @throws IOException
     */
    void doAction(WebSocket webSocket, WsRequest msg) throws IOException;

    /**
     * 消息类型
     *
     * @return
     */
    int getCommand();
}
