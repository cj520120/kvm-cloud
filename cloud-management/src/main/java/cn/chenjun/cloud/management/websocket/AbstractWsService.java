package cn.chenjun.cloud.management.websocket;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import cn.chenjun.cloud.management.util.SpringContextUtils;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.websocket.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractWsService implements ApplicationContextAware {
    private Session session;

    private static List<WsAction> ACTION_LIST;

    @SneakyThrows
    @OnOpen
    public void onConnect(Session session) {
        this.session = session;
    }

    @OnError
    public void onError(Session session, Throwable error) {
        SpringContextUtils.getBean(WsManager.class).unRegister(session);
    }

    @SneakyThrows
    @OnMessage
    public void onMessage(String jsonMsg) {

        WsRequest<?> msg = GsonBuilderUtil.create().fromJson(jsonMsg, WsRequest.class);
        ACTION_LIST.stream().filter(t -> Objects.equals(t.getType(), msg.getType())).forEach(t -> {
            try {
                t.doAction(session, msg);
            } catch (IOException ignored) {

            }
        });

    }

    @OnClose
    public void onClose() {
        SpringContextUtils.getBean(WsManager.class).unRegister(session);
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ACTION_LIST =new ArrayList<>(applicationContext.getBeansOfType(WsAction.class).values());
    }
}
