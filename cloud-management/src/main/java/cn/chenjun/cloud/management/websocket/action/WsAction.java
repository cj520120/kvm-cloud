package cn.chenjun.cloud.management.websocket.action;

import cn.chenjun.cloud.management.websocket.message.WsRequest;

import javax.websocket.Session;
import java.io.IOException;

public interface WsAction {
    void doAction(Session session, WsRequest<?> msg) throws IOException;

    int getType();
}
