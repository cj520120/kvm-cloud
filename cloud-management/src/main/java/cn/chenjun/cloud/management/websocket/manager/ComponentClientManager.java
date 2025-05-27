package cn.chenjun.cloud.management.websocket.manager;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.management.websocket.client.WebSocket;
import cn.chenjun.cloud.management.websocket.message.NotifyData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ComponentClientManager {
    public static final Map<Integer, List<WebSocket>> COMPONENT_CLIENT_MAP = new HashMap<>();

    public static void addComponentClient(int componentId, WebSocket webSocket) {
        synchronized (COMPONENT_CLIENT_MAP) {
            List<WebSocket> wsList = COMPONENT_CLIENT_MAP.computeIfAbsent(componentId, k -> new CopyOnWriteArrayList<>());
            wsList.add(webSocket);
        }
        webSocket.onClose.addEvent((sender, obj) -> {
            synchronized (COMPONENT_CLIENT_MAP) {
                List<WebSocket> wsList = COMPONENT_CLIENT_MAP.get(componentId);
                if (wsList == null) {
                    return;
                }
                wsList.remove(webSocket);
                if (wsList.isEmpty()) {
                    COMPONENT_CLIENT_MAP.remove(componentId);
                }
            }
        });

    }

    public static synchronized <T> void send(int componentId, NotifyData<T> message) {
        WsMessage<NotifyData<T>> wsMessage = WsMessage.<NotifyData<T>>builder().command(cn.chenjun.cloud.common.util.Constant.SocketCommand.WEB_NOTIFY).data(message).build();
        List<WebSocket> wsList;
        synchronized (COMPONENT_CLIENT_MAP) {
            wsList = new ArrayList<>(COMPONENT_CLIENT_MAP.getOrDefault(componentId, new ArrayList<>()));
        }
        wsList.forEach((webSocket) -> {
            try {
                webSocket.send(wsMessage);
            } catch (Exception ignored) {
                webSocket.close();
            }
        });
    }

}
