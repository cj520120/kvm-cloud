package cn.chenjun.cloud.management.websocket.action;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.management.util.SpringContextUtils;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Objects;
@Slf4j
public class ActionDispatcher {
    private static final Collection<WsAction> ACTION_LIST;

    static {
        ACTION_LIST = SpringContextUtils.getBeanCollection(WsAction.class);
    }

    @SneakyThrows
    public static void dispatch(Client webSocket, WsMessage msg) {
        webSocket.setLastActiveTime(System.currentTimeMillis());
        for (WsAction action : ACTION_LIST) {
            if (Objects.equals(action.getCommand(), msg.getCommand())) {
                try {
                    action.doAction(webSocket, msg);
                }catch (Exception e) {
                    log.error("Websocket 处理数据失败.", e);
                }
            }
        }
    }

}
