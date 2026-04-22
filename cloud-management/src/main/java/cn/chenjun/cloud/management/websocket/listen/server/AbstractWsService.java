package cn.chenjun.cloud.management.websocket.listen.server;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.FunctionUtils;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ObjectUtils;

import javax.websocket.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenjun
 */
@Slf4j
public abstract class AbstractWsService<T> {

    private static final ConcurrentHashMap<String, Client> WEBSOCKET_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class, List<Client>> CLIENT_CACHE = new ConcurrentHashMap<>();
    private final MessageProcess<T> processor;

    public AbstractWsService(MessageProcess<T> processor) {
        this.processor = processor;
    }

    @SneakyThrows
    @OnOpen
    public final void onOpen(Session session) {
        Client webSocket = this.createWebSocket(session);
        session.addMessageHandler(this.processor.create(webSocket));
        WEBSOCKET_CACHE.put(session.getId(), webSocket);
        this.register(webSocket);
        this.onConnection(webSocket);

    }

    protected abstract Client createWebSocket(Session session);

    protected void register(Client client) {
        log.info("websocket register:{}", client.getSessionId());
        List<Client> clients = CLIENT_CACHE.computeIfAbsent(this.getClass(), (k) -> Collections.synchronizedList(new ArrayList<>()));
        client.registerOnClose((s, e) -> {
            log.info("websocket unregister:{}", client.getSessionId());
            clients.remove(client);
        });
        clients.add(client);
    }

    @OnError
    public final void onError(Session session, Throwable error) {
        log.error("websocket error.closed", error);
        this.onClose(session);
    }


    @SneakyThrows
    @OnClose
    public final void onClose(Session session) {
        Client webSocket = WEBSOCKET_CACHE.remove(session.getId());
        if (webSocket != null) {
            webSocket.close();
        }
    }

    protected void onConnection(Client webSocket) {

    }

    protected abstract int getSocketType();

    protected abstract int getTimeoutSeconds();

    @Scheduled(fixedDelay = 1000)
    public void check() {
        if (this.getTimeoutSeconds() <= 0) {
            return;
        }
        List<Client> clients = CLIENT_CACHE.get(this.getClass());
        if (ObjectUtils.isEmpty(clients)) {
            return;
        }
        List<Client> checkClients = new ArrayList<>(clients);
        checkClients.forEach(client -> {
            int expireCount = Math.toIntExact((System.currentTimeMillis() - client.getLastActiveTime()) / (this.getTimeoutSeconds() * 1000L));
            if (expireCount > 3) {
                log.info("关闭超时连接:{},sessionId={}", client, client.getSessionId());
                FunctionUtils.ignoreRun(client::close);
                clients.remove(client);
            } else {
                long lastPingTime = client.getLastPingTime();
                if ((System.currentTimeMillis() - lastPingTime) / 1000 >= this.getTimeoutSeconds()) {
                    log.debug("发送心跳包:{},sessionId={}", client, client.getSessionId());
                    FunctionUtils.ignoreRun(() -> client.sendCommand(Constant.SocketCommand.PING));
                    client.setLastPingTime(System.currentTimeMillis());
                }
            }
        });
    }

    @FunctionalInterface
    protected interface MessageProcess<T> {
        MessageHandler.Whole<T> create(Client webSocket);
    }
}
