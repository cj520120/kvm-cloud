package cn.chenjun.cloud.agent.ws;

import cn.chenjun.cloud.agent.operate.bean.SubmitTask;
import cn.chenjun.cloud.agent.util.ClientService;
import cn.chenjun.cloud.agent.ws.client.WsClient;
import cn.chenjun.cloud.agent.ws.handler.PacketHandler;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.util.Constant;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SessionManager {
    @Autowired
    private ClientService clientService;
    @Autowired
    private List<PacketHandler> packetHandlers;
    private WsClient session = null;

    @Scheduled(fixedRate = 10000)
    @Synchronized
    public void checkConnect() {
        if (this.session == null) {
            if (!this.clientService.isInit()) {
                return;
            }
            try {
                String serverUrl = this.clientService.getManagerUri();
                if (serverUrl.endsWith("/")) {
                    serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
                }
                serverUrl = serverUrl.replace("http://", "ws://").replace("https://", "wss://");
                serverUrl += "/api/host/ws";
                this.session = new WsClient(new URI(serverUrl));
                this.session.connect();
                log.info("尝试连接管理端，url:{}", serverUrl);
                this.session.onConnect.addEvent((sender, obj) -> {
                    log.info("服务器连接成功");
                });
                this.session.onMessage.addEvent((sender, obj) -> {
                    WsMessage<byte[]> wsMessage = obj.getEvent();
                    List<PacketHandler> handlers = this.packetHandlers.stream().filter(handler -> handler.getCommand() == wsMessage.getCommand()).collect(Collectors.toList());
                    if (handlers.isEmpty()) {
                        log.warn("未找到对应的处理器，command:{}", wsMessage.getCommand());
                    } else {
                        for (PacketHandler handler : handlers) {
                            log.debug("收到消息，command:{},处理器={}", wsMessage.getCommand(), handler.getClass().getName());
                            try {
                                handler.process((WsClient) sender, wsMessage);
                            } catch (Exception e) {
                                log.error("处理器执行异常，command:{},处理器={}", wsMessage.getCommand(), handler.getClass().getName(), e);
                            }
                        }
                    }

                });
                this.session.onClose.addEvent((sender, obj) -> {
                    if (Objects.equals(this.session, sender)) {
                        log.info("连接断开，稍后重试...");
                        this.close();
                    }
                });
            } catch (Exception e) {
                log.error("连接管理端，认证失败", e);
                this.close();
            }
        } else {
            this.sendHeartbeat();
        }
    }

    public void sendHeartbeat() {
        if (!this.isConnected()) {
            return;
        }
        try {
            this.session.sendCommand(Constant.SocketCommand.AGENT_HEART_BEAT);
        } catch (Exception e) {
            log.error("发送心跳失败", e);
            this.close();
        }
    }


    public void close() {
        log.info("关闭连接");
        if (this.session != null) {
            try {
                this.session.close();
            } catch (Exception ignored) {

            }
        }
        this.session = null;
    }

    public boolean submitTask(SubmitTask submitTask) {
        if (!this.isConnected()) {
            return false;
        }
        try {
            this.session.sendJson(Constant.SocketCommand.AGENT_TASK_CALLBACK, submitTask);
            log.info("上报任务成功:taskId={},data={}", submitTask.getTaskId(), submitTask.getData());
            return true;
        } catch (Exception e) {
            log.error("上报任务失败", e);
            return false;
        }
    }

    public boolean isConnected() {
        return this.session != null && this.session.isLogin();
    }

}