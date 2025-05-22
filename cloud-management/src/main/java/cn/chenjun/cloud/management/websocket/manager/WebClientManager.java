package cn.chenjun.cloud.management.websocket.manager;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.management.websocket.client.WebSocket;
import cn.chenjun.cloud.management.websocket.message.NotifyData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebClientManager {
    public final static Map<Integer, List<WebSocket>> WEB_CLIENT_MAP = new HashMap<>();

    public static void addClient(int userId, WebSocket webSocket) {
        synchronized (WEB_CLIENT_MAP) {
            List<WebSocket> userList = WEB_CLIENT_MAP.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
            userList.add(webSocket);
        }
        webSocket.onClose.addEvent((sender, event) -> {
            synchronized (WEB_CLIENT_MAP) {
                List<WebSocket> userList = WEB_CLIENT_MAP.get(userId);
                if (userList == null) {
                    return;
                }
                userList.remove(webSocket);
                if (userList.isEmpty()) {
                    WEB_CLIENT_MAP.remove(userId);
                }
            }
        });
    }

    public static synchronized <T> void sendAll(NotifyData<T> message) {
        WsMessage<NotifyData<T>> wsMessage = WsMessage.<NotifyData<T>>builder().command(cn.chenjun.cloud.common.util.Constant.SocketCommand.WEB_NOTIFY).data(message).build();
        List<WebSocket> userList = new ArrayList<>();
        synchronized (WEB_CLIENT_MAP) {
            for (List<WebSocket> webSockets : WEB_CLIENT_MAP.values()) {
                userList.addAll(webSockets);
            }
        }
        userList.parallelStream().forEach(ws -> {
            try {
                ws.send(wsMessage);
            } catch (Exception ignored) {
                ws.close();
            }
        });
    }
}
