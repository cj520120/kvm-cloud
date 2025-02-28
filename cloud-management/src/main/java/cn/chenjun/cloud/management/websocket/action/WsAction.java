package cn.chenjun.cloud.management.websocket.action;

import cn.chenjun.cloud.management.websocket.message.WsRequest;

import javax.websocket.Session;
import java.io.IOException;

/**
 * @author chenjun
 */
public interface WsAction {
    /**
     * ws 消息处理
     *
     * @param session
     * @param msg
     * @throws IOException
     */
    void doAction(Session session, WsRequest msg) throws IOException;

    /**
     * 消息类型
     *
     * @return
     */
    int getCommand();
}
