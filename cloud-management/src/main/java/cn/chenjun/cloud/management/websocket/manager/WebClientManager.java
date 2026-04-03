package cn.chenjun.cloud.management.websocket.manager;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.message.NotifyData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebClientManager {
    public final static Map<Integer, List<Client>> WEB_CLIENT_MAP = new HashMap<>();

    public static void addClient(int userId, Client webSocket) {
        synchronized (WEB_CLIENT_MAP) {
            List<Client> userList = WEB_CLIENT_MAP.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
            userList.add(webSocket);
        }
        webSocket.registerOnClose((sender, event) -> {
            synchronized (WEB_CLIENT_MAP) {
                List<Client> userList = WEB_CLIENT_MAP.get(userId);
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
        List<Client> userList = new ArrayList<>();
        synchronized (WEB_CLIENT_MAP) {
            for (List<Client> webSockets : WEB_CLIENT_MAP.values()) {
                userList.addAll(webSockets);
            }
        }
        userList.parallelStream().forEach(ws -> {
            try {
                ws.sendJsonPacket(wsMessage);
            } catch (Exception ignored) {
                try {
                    ws.close();
                } catch (Exception ignored2) {
                }
            }
        });
    }
}
