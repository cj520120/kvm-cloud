package cn.chenjun.cloud.management.websocket.listen;

import cn.chenjun.cloud.management.websocket.util.WsSessionManager;
import lombok.SneakyThrows;

import javax.websocket.*;

/**
 * @author chenjun
 */
public abstract class AbstractWsService<T> {


    @SneakyThrows
    @OnOpen
    public void onConnect(Session session) {
        session.addMessageHandler(createMessageHandler(session));
    }

    @OnError
    public void onError(Session session, Throwable error) {
        WsSessionManager.unRegister(session);
    }


    @SneakyThrows
    @OnClose
    public void onClose(Session session) {
        WsSessionManager.unRegister(session);
    }

    protected abstract MessageHandler.Whole<T> createMessageHandler(Session session);
}
