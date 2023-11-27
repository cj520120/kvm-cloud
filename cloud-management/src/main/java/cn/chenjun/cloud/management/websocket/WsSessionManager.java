package cn.chenjun.cloud.management.websocket;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.websocket.client.WsClient;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.hutool.core.collection.ConcurrentHashSet;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.Objects;

/**
 * @author chenjun
 */
@Component
public class WsSessionManager {
    public ConcurrentHashSet<WsClient> wsClientSets = new ConcurrentHashSet<>();


    public synchronized WsClient registerWebClient(Session session) {
        WsClient ws = WsClient.builder().session(session).clientType(Constant.WsClientType.WEB).build();
        this.wsClientSets.add(ws);
        return ws;
    }

    public synchronized WsClient registerComponentClient(Session session, int networkId) {
        WsClient ws = WsClient.builder().session(session).clientType(Constant.WsClientType.COMPONENT).networkId(networkId).build();
        this.wsClientSets.add(ws);
        return ws;
    }

    public synchronized void unRegister(Session session) {
        this.wsClientSets.removeIf(t -> Objects.equals(session, t.getSession()));
    }

    public synchronized WsClient getClient(Session session) {
        return wsClientSets.stream().filter(ws -> Objects.equals(session, ws.getSession())).findFirst().orElse(null);
    }

    public synchronized <T> void sendWebNotify(NotifyData<T> message) {

        WsMessage<NotifyData<T>> wsMessage = WsMessage.<NotifyData<T>>builder().command(cn.chenjun.cloud.common.util.Constant.SocketCommand.WEB_NOTIFY).data(message).build();
        wsClientSets.stream().filter(ws -> Objects.equals(ws.getClientType(), Constant.WsClientType.WEB)).forEach(ws -> {
            try {
                ws.send(wsMessage);
            } catch (Exception ignored) {

            }
        });
    }

    public synchronized <T> void sendComponentNotify(int networkId, NotifyData<T> message) {

        WsMessage<NotifyData<T>> wsMessage = WsMessage.<NotifyData<T>>builder().command(cn.chenjun.cloud.common.util.Constant.SocketCommand.COMPONENT_NOTIFY).data(message).build();
        wsClientSets.stream().filter(ws -> Objects.equals(networkId, ws.getNetworkId()) && Objects.equals(ws.getClientType(), Constant.WsClientType.COMPONENT)).forEach(ws -> {
            try {
                ws.send(wsMessage);
            } catch (Exception ignored) {
            }
        });
    }

}
