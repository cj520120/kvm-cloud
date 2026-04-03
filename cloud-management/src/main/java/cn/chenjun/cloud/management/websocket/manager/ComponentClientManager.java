package cn.chenjun.cloud.management.websocket.manager;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.listen.context.ComponentContext;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class ComponentClientManager {
    public static final Map<Integer, List<Client>> COMPONENT_CLIENT_MAP = new HashMap<>();

    public static void addComponentClient(int componentId, Client webSocket) {
        synchronized (COMPONENT_CLIENT_MAP) {
            List<Client> wsList = COMPONENT_CLIENT_MAP.computeIfAbsent(componentId, k -> new CopyOnWriteArrayList<>());
            Iterator<Client> iterator = wsList.iterator();
            ComponentContext sourceContext = (ComponentContext) webSocket.getContext();
            while (iterator.hasNext()) {
                Client ws = iterator.next();
                ComponentContext context = (ComponentContext) ws.getContext();
                if (Objects.equals(context.getComponentGuestId(), sourceContext.getComponentId())) {
                    iterator.remove();
                    try {
                        log.info("close old component client {}", context.getComponentGuestId());
                        ws.close();
                    } catch (Exception ignored) {

                    }
                }
            }
            wsList.add(webSocket);
        }
        webSocket.registerOnClose((sender, obj) -> {
            synchronized (COMPONENT_CLIENT_MAP) {
                List<Client> wsList = COMPONENT_CLIENT_MAP.get(componentId);
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

    public static synchronized <T> void send(int componentId, NotifyData<T> message, int command) {
        WsMessage<NotifyData<T>> wsMessage = WsMessage.<NotifyData<T>>builder().command(command).data(message).build();
        List<Client> wsList;
        synchronized (COMPONENT_CLIENT_MAP) {
            wsList = new ArrayList<>(COMPONENT_CLIENT_MAP.getOrDefault(componentId, new ArrayList<>()));
        }
        wsList.forEach((webSocket) -> {
            try {
                webSocket.sendJsonPacket(wsMessage);
            } catch (Exception ignored) {
                try {
                    webSocket.close();
                } catch (Exception e) {
                }
            }
        });
    }

    @Scheduled(fixedDelay = 10000)
    public void checkKeep() {
        List<Client> wsList = new ArrayList<>();
        synchronized (COMPONENT_CLIENT_MAP) {
            COMPONENT_CLIENT_MAP.forEach((componentId, webSocketList) -> {
                wsList.addAll(webSocketList);
            });
        }
        for (Client ws : wsList) {
            if (ws.getLastActiveTime() < System.currentTimeMillis() - 1000 * 60) {
                try {
                    ws.close();
                } catch (Exception ignored) {

                }

            }
        }
    }

}
