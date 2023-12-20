package cn.chenjun.cloud.management.websocket;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.websocket.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author chenjun
 */
public abstract class AbstractWsService implements ApplicationContextAware {
    private static List<WsAction> ACTION_LIST;
    private static WsSessionManager SESSION_MANAGER;
    private Session session;

    @SneakyThrows
    @OnOpen
    public void onConnect(Session session) {
        this.session = session;
    }

    @OnError
    public void onError(Session session, Throwable error) {
        SESSION_MANAGER.unRegister(session);
    }

    @SneakyThrows
    @OnMessage
    public void onMessage(String jsonMsg) {

        WsRequest msg = GsonBuilderUtil.create().fromJson(jsonMsg, WsRequest.class);
        ACTION_LIST.stream().filter(t -> Objects.equals(t.getCommand(), msg.getCommand())).forEach(t -> {
            try {
                t.doAction(session, msg);
            } catch (IOException ignored) {

            }
        });

    }

    @OnClose
    public void onClose() {
        SESSION_MANAGER.unRegister(session);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ACTION_LIST = new ArrayList<>(applicationContext.getBeansOfType(WsAction.class).values());
        SESSION_MANAGER = applicationContext.getBean(WsSessionManager.class);
    }
}
