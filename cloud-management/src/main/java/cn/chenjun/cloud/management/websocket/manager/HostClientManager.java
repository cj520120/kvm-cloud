package cn.chenjun.cloud.management.websocket.manager;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

@Slf4j
public class HostClientManager {
    public static final Map<Integer, Client> HOST_CLIENT_MAP = new HashMap<>();

    public static void addHost(int hostId, Client webSocket) {
        synchronized (HOST_CLIENT_MAP) {
            HOST_CLIENT_MAP.put(hostId, webSocket);
        }

        webSocket.registerOnClose((sender, obj) -> {
            synchronized (HOST_CLIENT_MAP) {
                if (Objects.equals(HOST_CLIENT_MAP.get(hostId), webSocket)) {
                    HOST_CLIENT_MAP.remove(hostId);
                }
            }
        });

    }


    public static synchronized <T> void send(int hostId, int command, byte[] data) {
        Client webSocket = HOST_CLIENT_MAP.get(hostId);
        if (webSocket != null) {
            try {
                webSocket.sendBinaryPacket(command, data);
            } catch (Exception e) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "发送消息到主机失败", e);
            }
        } else {
            throw new CodeException(ErrorCode.HOST_NOT_READY, "主机未就绪");
        }
    }

    public static Client getHost(int hostId) {
        return HOST_CLIENT_MAP.get(hostId);
    }

    @Scheduled(fixedDelay = 10000)
    public void checkKeep() {
        List<Client> wsList = new ArrayList<>();
        synchronized (HOST_CLIENT_MAP) {
            wsList.addAll(HOST_CLIENT_MAP.values());
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
