package cn.chenjun.cloud.management.websocket.action;

import cn.chenjun.cloud.management.util.SpringContextUtils;
import cn.chenjun.cloud.management.websocket.message.WsRequest;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

public class ActionDispatcher {
    private static final Collection<WsAction> ACTION_LIST;

    static {
        ACTION_LIST = SpringContextUtils.getBeanCollection(WsAction.class);
    }

    public static void dispatch(Session session, WsRequest msg) throws IOException {

        for (WsAction action : ACTION_LIST) {
            if (Objects.equals(action.getCommand(), msg.getCommand())) {
                action.doAction(session, msg);
                return;
            }
        }
    }

}
