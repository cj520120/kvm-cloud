package cn.chenjun.cloud.agent.ws;

import cn.chenjun.cloud.agent.operate.bean.SubmitTask;
import cn.chenjun.cloud.agent.util.ClientService;
import cn.chenjun.cloud.agent.ws.client.WsClient;
import cn.chenjun.cloud.agent.ws.handler.PacketHandler;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SessionManager implements Closeable {

    @Autowired
    private ClientService clientService;

    @Autowired
    private List<PacketHandler> packetHandlers;
    private WsClient session = null;
    private final Object sessionLock = new Object();

    @Scheduled(fixedRate = 5000)
    public void checkConnect() {
        synchronized (sessionLock) {
            if (session == null) {
                if (!clientService.isInit()) {
                    log.info("管理端未初始化，不尝试连接");
                    return;
                }
                try {
                    String serverUrl = buildWsUrl();
                    log.info("尝试连接管理端，url: {}", serverUrl);
                    WsClient newSession = new WsClient(new URI(serverUrl));
                    registerEventListeners(newSession);
                    newSession.connect();
                    this.session = newSession;
                    log.info("WebSocket 连接已建立");
                } catch (Exception e) {
                    log.error("连接管理端失败", e);
                    closeSessionInternal();
                }
            } else {
                log.debug("心跳间隔已超过，发送心跳...");
                sendHeartbeat();
            }
        }
    }

    private String buildWsUrl() throws URISyntaxException {
        String managerUri = clientService.getManagerUri();
        URI original = new URI(managerUri);
        String scheme = "ws".equals(original.getScheme()) ? "ws" :
                ("https".equals(original.getScheme()) ? "wss" : "ws");
        String path = "/api/host/ws";
        URI wsUri = new URI(scheme, original.getUserInfo(), original.getHost(), original.getPort(), path, null, null);
        return wsUri.toString();
    }

    /**
     * 注册 WebSocket 事件监听器
     */
    private void registerEventListeners(WsClient client) {
        client.onConnect.addEvent((sender, obj) -> {
            log.info("管理端 WebSocket 连接成功，开始登录...");
        });
        client.onMessage.addEvent((sender, obj) -> {
            WsMessage<byte[]> wsMessage = obj.getEvent();
            List<PacketHandler> handlers = packetHandlers.stream()
                    .filter(handler -> handler.getCommand() == wsMessage.getCommand())
                    .collect(Collectors.toList());
            if (handlers.isEmpty()) {
                log.warn("未找到对应的处理器，command: {}", wsMessage.getCommand());
            } else {
                for (PacketHandler handler : handlers) {
                    log.debug("收到消息，command: {}, 处理器: {}", wsMessage.getCommand(), handler.getClass().getName());
                    try {
                        handler.process((WsClient) sender, wsMessage);
                    } catch (Exception e) {
                        log.error("处理器执行异常，command: {}, 处理器: {}", wsMessage.getCommand(), handler.getClass().getName(), e);
                    }
                }
            }
        });

        // 连接关闭事件
        client.onClose.addEvent((sender, obj) -> {
            log.info("WebSocket 连接关闭事件触发");
            synchronized (sessionLock) {
                if (session == sender) {
                    log.info("连接断开，稍后重连...");
                    closeSessionInternal();
                }
            }
        });
    }


    /**
     * 发送心跳（仅在连接正常时调用，需持有 sessionLock）
     */
    private void sendHeartbeat() {
        if (session == null || !session.isLogin()) {
            return;
        }
        try {
            session.sendCommand(Constant.SocketCommand.AGENT_HEART_BEAT);
            log.trace("发送心跳");
        } catch (Exception e) {
            log.error("发送心跳失败", e);
            closeSessionInternal();
        }
    }

    private void closeSessionInternal() {
        WsClient toClose = null;
        synchronized (sessionLock) {
            if (this.session != null) {
                toClose = this.session;
                this.session = null;
            }
        }
        if (toClose != null) {
            try {
                toClose.close();
                log.info("已关闭 WebSocket 连接");
            } catch (Exception e) {
                log.warn("关闭连接时发生异常", e);
            }
        }
    }

    @Override
    public void close() {
        closeSessionInternal();
    }

    /**
     * 上报任务结果
     *
     * @param submitTask 任务数据
     * @return true 如果发送成功，false 如果当前未连接或发送失败
     */
    public boolean submitTask(SubmitTask submitTask) {
        WsClient currentSession;
        synchronized (sessionLock) {
            currentSession = this.session;
        }
        if (currentSession == null || !currentSession.isLogin()) {
            log.warn("submitTask 失败：WebSocket 未连接，taskId={}", submitTask.getTaskId());
            return false;
        }
        try {
            currentSession.sendJson(Constant.SocketCommand.AGENT_TASK_CALLBACK, submitTask);
            log.info("上报任务成功: taskId={}, data={}", submitTask.getTaskId(), submitTask.getData());
            return true;
        } catch (Exception e) {
            log.error("上报任务失败: taskId={}", submitTask.getTaskId(), e);
            closeSessionInternal();
            return false;
        }
    }

    /**
     * 判断是否已连接并登录
     */
    public boolean isConnected() {
        synchronized (sessionLock) {
            return session != null && session.isLogin();
        }
    }
}