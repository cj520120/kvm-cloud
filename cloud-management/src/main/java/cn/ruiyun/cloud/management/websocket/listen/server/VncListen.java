package cn.ruiyun.cloud.management.websocket.listen.server;

import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.event.EventHandler;
import cn.ruiyun.cloud.common.socket.packet.WsMessage;
import cn.ruiyun.cloud.common.socket.packet.data.base.MapData;
import cn.ruiyun.cloud.common.socket.packet.data.host.VncConnect;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.common.util.FunctionUtils;
import cn.ruiyun.cloud.common.util.SecurityUtil;
import cn.ruiyun.cloud.management.config.ApplicationConfig;
import cn.ruiyun.cloud.management.servcie.bean.MemGraphicsInfo;
import cn.ruiyun.cloud.management.util.RedisKeyUtil;
import cn.ruiyun.cloud.management.util.SpringContextUtils;
import cn.ruiyun.cloud.management.websocket.action.ActionDispatcher;
import cn.ruiyun.cloud.management.websocket.common.SocketType;
import cn.ruiyun.cloud.management.websocket.listen.client.Client;
import cn.ruiyun.cloud.management.websocket.listen.client.NodeSocket;
import cn.ruiyun.cloud.management.websocket.listen.client.VncSocket;
import cn.ruiyun.cloud.management.websocket.listen.codec.VncCodecHandler;
import cn.ruiyun.cloud.management.websocket.listen.context.ConnectContext;
import cn.ruiyun.cloud.management.websocket.listen.context.HostContext;
import cn.ruiyun.cloud.management.websocket.listen.context.VncContext;
import cn.ruiyun.cloud.management.websocket.mgr.VncManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author chenjun
 */
@Slf4j
@ServerEndpoint(value = "/api/vnc")
@Component
public class VncListen extends AbstractWsService<WsMessage<byte[]>, ByteBuffer> {
    public VncListen() {
        super(VncCodecHandler::new);
    }

    @Override
    protected Client createWebSocket(Session session) {
        VncSocket webSocket = new VncSocket(session);
        List<String> tokens = session.getRequestParameterMap().get("token");
        if (ObjectUtils.isEmpty(tokens)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "缺少token参数");
        }
        String token = tokens.get(0);
        RedissonClient redissonClient = SpringContextUtils.getBean(RedissonClient.class);
        RBucket<MemGraphicsInfo> bucket = redissonClient.getBucket(RedisKeyUtil.getGuestGraphicsToken(token));
        MemGraphicsInfo memGraphicsInfo = bucket.getAndDelete();
        if (Objects.isNull(memGraphicsInfo)) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "token无效");
        }
        webSocket.putAttribute("vnc", memGraphicsInfo);
        return webSocket;
    }

    @Override
    protected int getTimeoutSeconds() {
        return 30;
    }

    @Override
    protected int getSocketType() {
        return SocketType.VNC_SOCKET;
    }

    @SneakyThrows
    @Override
    protected void onConnection(Client webSocket) {
        super.onConnection(webSocket);
        MemGraphicsInfo memGraphicsInfo = (MemGraphicsInfo) webSocket.getAttribute("vnc");
        RedissonClient redissonClient = SpringContextUtils.getBean(RedissonClient.class);
        ApplicationConfig applicationConfig = SpringContextUtils.getBean(ApplicationConfig.class);
        if (memGraphicsInfo.getHostId() <= 0) {
            webSocket.close();
            log.info("Vnc连接失败，虚拟机未运行或不存在.guest={}", memGraphicsInfo.getGuestName());
            return;
        }
        String sessionId = UUID.randomUUID().toString();
        VncContext vncContext = VncContext.builder().id(sessionId).build();
        webSocket.login(vncContext);
        VncConnect vncConnect = new VncConnect();
        vncConnect.setHostId(memGraphicsInfo.getHostId());
        vncConnect.setName(memGraphicsInfo.getGuestName());
        vncConnect.setId(sessionId);

        String hostKey = RedisKeyUtil.getHostConnectionKey(memGraphicsInfo.getHostId());
        HostContext hostContext = (HostContext) redissonClient.getBucket(hostKey).get();
        if (hostContext == null) {
            FunctionUtils.ignoreRun(webSocket::close);
            log.info("Vnc连接失败，宿主机不在线.guest={},hostId={}", memGraphicsInfo.getGuestName(), memGraphicsInfo.getHostId());
            return;
        }
        if (Objects.equals(applicationConfig.getCluster().getNodeUrl(), hostContext.getNodeUrl())) {
            log.info("当前主机在本节点,无需转发");
            WsMessage<byte[]> msg = WsMessage.<byte[]>builder().command(Constant.SocketCommand.VNC_CONNECT).data(vncConnect.toBytes()).build();
            ActionDispatcher.dispatch(webSocket, msg);
            return;
        }
        String nodeUrl = hostContext.getNodeUrl();
        if (nodeUrl.endsWith("/")) {
            nodeUrl = nodeUrl.substring(0, nodeUrl.length() - 1);
        }
        nodeUrl = nodeUrl.replaceFirst("http://", "ws://").replaceFirst("https://", "wss://");
        URI url = new URI(nodeUrl + "/api/node/ws");
        NodeSocket nodeSocket = new NodeSocket(url);
        try {
            MapData mapData = new MapData();
            mapData.put("timestamp", String.valueOf(System.currentTimeMillis()));
            mapData.put("nonce", UUID.randomUUID().toString());
            mapData.put("sign", SecurityUtil.signature(mapData, applicationConfig.getCluster().getToken()));
            nodeSocket.putAttribute("vnc", memGraphicsInfo);
            nodeSocket.connect();
            nodeSocket.sendBinaryPacket(Constant.SocketCommand.NODE_REGISTER, mapData.toBytes());
            VncManager.connect(webSocket, nodeSocket, vncConnect);
            EventHandler<ConnectContext> onCloseHandler = (sender, obj) -> {
                FunctionUtils.ignoreRun(webSocket::close);
                FunctionUtils.ignoreRun(nodeSocket::close);
            };
            nodeSocket.registerOnClose(onCloseHandler);
            webSocket.registerOnClose(onCloseHandler);
            this.register(nodeSocket);
        } catch (Exception e) {
            log.error("Tcp客户端连接节点失败", e);
            FunctionUtils.ignoreRun(webSocket::close);
            FunctionUtils.ignoreRun(nodeSocket::close);
        }
    }


}
