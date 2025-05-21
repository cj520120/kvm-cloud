package cn.chenjun.cloud.management.websocket.util;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.websocket.client.WsClient;
import cn.chenjun.cloud.management.websocket.client.owner.ComponentWsOwner;
import cn.chenjun.cloud.management.websocket.client.owner.WebWsOwner;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.hutool.core.collection.ConcurrentHashSet;

import javax.websocket.Session;
import java.util.Objects;

/**
 * @author chenjun
 */
public class WsSessionManager {
    public static final ConcurrentHashSet<WsClient> SESSION_LIST = new ConcurrentHashSet<>();


    public static synchronized WsClient registerWebClient(Session session, int userId) {
        unRegister(session);
        WsClient<WebWsOwner> ws = WsClient.<WebWsOwner>builder().session(session).owner(WebWsOwner.builder().userId(userId).build()).build();
        SESSION_LIST.add(ws);
        return ws;
    }

    public static synchronized WsClient registerComponentClient(Session session, int networkId, int componentId) {
        unRegister(session);
        WsClient<ComponentWsOwner> ws = WsClient.<ComponentWsOwner>builder().session(session).owner(ComponentWsOwner.builder().networkId(networkId).componentId(componentId).build()).build();
        SESSION_LIST.add(ws);
        return ws;
    }

    public static synchronized void unRegister(Session session) {
        SESSION_LIST.removeIf(t -> Objects.equals(session, t.getSession()));
    }

    public static synchronized WsClient getClient(Session session) {
        return SESSION_LIST.stream().filter(ws -> Objects.equals(session, ws.getSession())).findFirst().orElse(null);
    }

    public static synchronized <T> void sendWebNotify(NotifyData<T> message) {
        WsMessage<NotifyData<T>> wsMessage = WsMessage.<NotifyData<T>>builder().command(cn.chenjun.cloud.common.util.Constant.SocketCommand.WEB_NOTIFY).data(message).build();
        String msg = GsonBuilderUtil.create().toJson(wsMessage);
        SESSION_LIST.stream().filter(ws -> Objects.equals(ws.getOwner().getType(), Constant.WsClientType.WEB)).forEach(ws -> {
            try {
                ws.getSession().getBasicRemote().sendText(msg);
            } catch (Exception ignored) {

            }
        });
    }

    public static synchronized <T> void sendComponentNotify(int componentId, NotifyData<T> message) {
        WsMessage<NotifyData<T>> wsMessage = WsMessage.<NotifyData<T>>builder().command(cn.chenjun.cloud.common.util.Constant.SocketCommand.COMPONENT_NOTIFY).data(message).build();
        String msg = GsonBuilderUtil.create().toJson(wsMessage);
        for (WsClient ws : SESSION_LIST) {
            if (ws.getOwner() != null && ws.getOwner().getType() == Constant.WsClientType.COMPONENT) {
                ComponentWsOwner owner = (ComponentWsOwner) ws.getOwner();
                if (Objects.equals(owner.getComponentId(), componentId)) {
                    try {
                        ws.getSession().getBasicRemote().sendText(msg);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
}
