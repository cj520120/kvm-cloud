package cn.chenjun.cloud.management.websocket.mgr;

import cn.chenjun.cloud.common.event.EventHandler;
import cn.chenjun.cloud.common.socket.packet.data.host.VncConnect;
import cn.chenjun.cloud.common.socket.packet.data.host.VncData;
import cn.chenjun.cloud.common.socket.packet.data.host.VncDisconnect;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.FunctionUtils;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.listen.context.ConnectContext;
import cn.chenjun.cloud.management.websocket.manager.HostClientManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class VncManager {
    private static final Map<String, VncBind> vncMap = new ConcurrentHashMap<>();

    public static void connect(Client webSocket, VncConnect vncConnect) {
        VncConnect agentVncconnect = new VncConnect();
        agentVncconnect.setId(vncConnect.getId());
        agentVncconnect.setName(vncConnect.getName());

        Client agentWebSocket = HostClientManager.HOST_CLIENT_MAP.get(vncConnect.getHostId());
        if (agentWebSocket == null) {
            webSocket.sendBinaryPacket(Constant.SocketCommand.VNC_DISCONNECT, VncDisconnect.builder().id(vncConnect.getId()).build().toBytes());
            return;
        }
        EventHandler<ConnectContext> onCloseHandler = (sender, event) -> {
            disconnect(VncDisconnect.builder().id(agentVncconnect.getId()).build());
        };
        webSocket.registerOnClose(onCloseHandler);
        agentWebSocket.registerOnClose(onCloseHandler);
        VncBind bind = VncBind.builder().id(agentVncconnect.getId()).webSocket(webSocket).agentWebSocket(agentWebSocket).build();
        log.info("VNC建立绑定,id={},from={} to={}", agentVncconnect.getId(), webSocket.getSessionId(), agentWebSocket.getSessionId());
        VncBind oldBind = vncMap.put(agentVncconnect.getId(), bind);
        if (oldBind != null) {
            log.warn("VNC连接重复,id={}", agentVncconnect.getId());
            oldBind.disconnect(true);
        }
        try {
            agentWebSocket.sendBinaryPacket(Constant.SocketCommand.VNC_CONNECT, agentVncconnect.toBytes());
        } catch (Exception e) {
            log.error("发送VNC连接消息失败", e);
            disconnect(VncDisconnect.builder().id(vncConnect.getId()).build());
        }
    }

    public static void connect(Client webSocket, Client nodeSocket, VncConnect vncConnect) {
        VncConnect agentVncconnect = new VncConnect();
        agentVncconnect.setId(vncConnect.getId());
        agentVncconnect.setName(vncConnect.getName());
        agentVncconnect.setHostId(vncConnect.getHostId());
        EventHandler<ConnectContext> onCloseHandler = (sender, event) -> {
            disconnect(VncDisconnect.builder().id(agentVncconnect.getId()).build());
        };
        webSocket.registerOnClose(onCloseHandler);
        nodeSocket.registerOnClose(onCloseHandler);
        VncBind oldBind = vncMap.put(agentVncconnect.getId(), VncBind.builder().id(agentVncconnect.getId()).webSocket(webSocket).agentWebSocket(nodeSocket).build());
        log.info("VNC建立Node转发绑定,id={},from={} to={}", agentVncconnect.getId(), webSocket.getSessionId(), nodeSocket.getSessionId());
        if (oldBind != null) {
            log.warn("VNC连接重复,关闭旧连接,id={}", agentVncconnect.getId());
            oldBind.disconnect(true);
        }
        try {
            nodeSocket.sendBinaryPacket(Constant.SocketCommand.VNC_CONNECT, agentVncconnect.toBytes());
        } catch (Exception e) {
            log.error("发送VNC连接消息失败", e);
            disconnect(VncDisconnect.builder().id(vncConnect.getId()).build());
        }
    }


    public static void forward(Client webSocket, VncData data) {
        VncBind vncBind = vncMap.get(data.getId());
        if (Objects.isNull(vncBind)) {
            webSocket.sendBinaryPacket(Constant.SocketCommand.VNC_DISCONNECT, VncDisconnect.builder().id(data.getId()).build().toBytes());
        } else {
            vncBind.forward(webSocket, data);
        }
    }

    public static void disconnect(VncDisconnect data) {
        VncBind vncBind = vncMap.remove(data.getId());
        if (Objects.nonNull(vncBind)) {
            vncBind.disconnect(false);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class VncBind {
        private String id;
        private Client webSocket;
        private Client agentWebSocket;
        private boolean isClosed = false;

        public void disconnect(boolean isOld) {
            if (!isClosed) {
                isClosed = true;
                VncDisconnect data = VncDisconnect.builder().id(id).build();
                FunctionUtils.ignoreRun(() -> webSocket.sendBinaryPacket(Constant.SocketCommand.VNC_DISCONNECT, data.toBytes()));
                FunctionUtils.ignoreRun(() -> agentWebSocket.sendBinaryPacket(Constant.SocketCommand.VNC_DISCONNECT, data.toBytes()));
                if (!isOld) {
                    VncManager.disconnect(data);
                }

                log.info("VNC解除绑定,id={},from={} to={}", this.id, webSocket.getSessionId(), agentWebSocket.getSessionId());
            }
        }

        public void forward(Client socket, VncData data) {
            try {
                Client toClient = null;
                if (Objects.equals(webSocket, socket)) {
                    toClient = agentWebSocket;
                } else if (Objects.equals(agentWebSocket, socket)) {
                    toClient = webSocket;
                }
                if (toClient != null) {
                    log.debug("VNC转发,id={},from={},to={},length={}", data.getId(), socket.getSessionId(), toClient.getSessionId(), data.getData().length);
                    toClient.sendBinaryPacket(Constant.SocketCommand.VNC_DATA, data.toBytes());
                } else {
                    log.warn("VNC转发异常,未知连接,断开当前的链接");
                    disconnect(false);
                }
            } catch (Exception e) {
                log.error("VNC转发异常", e);
                disconnect(false);
            }
        }
    }
}
