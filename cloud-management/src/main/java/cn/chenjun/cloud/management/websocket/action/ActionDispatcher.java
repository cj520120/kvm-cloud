package cn.chenjun.cloud.management.websocket.action;

import cn.chenjun.cloud.management.util.SpringContextUtils;
import cn.chenjun.cloud.management.websocket.client.WebSocket;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import lombok.SneakyThrows;

import java.util.Collection;
import java.util.Objects;

public class ActionDispatcher {
    private static final Collection<WsAction> ACTION_LIST;

    static {
        ACTION_LIST = SpringContextUtils.getBeanCollection(WsAction.class);
    }

    @SneakyThrows
    public static void dispatch(WebSocket webSocket, WsRequest msg) {

        for (WsAction action : ACTION_LIST) {
            if (Objects.equals(action.getCommand(), msg.getCommand())) {
                action.doAction(webSocket, msg);
                return;
            }
        }
    }

}
