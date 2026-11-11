package cn.ruiyun.cloud.management.websocket.listen.server;

import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.socket.packet.WsMessage;
import cn.ruiyun.cloud.common.socket.packet.data.host.VncConnect;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.common.util.FunctionUtils;
import cn.ruiyun.cloud.management.config.ApplicationConfig;
import cn.ruiyun.cloud.management.servcie.CacheService;
import cn.ruiyun.cloud.management.servcie.bean.MemGraphicsInfo;
import cn.ruiyun.cloud.management.util.CacheKeyUtil;
import cn.ruiyun.cloud.management.util.SpringContextUtils;
import cn.ruiyun.cloud.management.websocket.action.ActionDispatcher;
import cn.ruiyun.cloud.management.websocket.common.SocketType;
import cn.ruiyun.cloud.management.websocket.listen.client.Client;
import cn.ruiyun.cloud.management.websocket.listen.client.VncSocket;
import cn.ruiyun.cloud.management.websocket.listen.codec.VncCodecHandler;
import cn.ruiyun.cloud.management.websocket.listen.context.HostContext;
import cn.ruiyun.cloud.management.websocket.listen.context.VncContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
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

        CacheService cacheService = SpringContextUtils.getBean(CacheService.class);
        MemGraphicsInfo memGraphicsInfo = cacheService.getAndRemove(CacheKeyUtil.getGuestGraphicsToken(token));

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
        CacheService cacheService = SpringContextUtils.getBean(CacheService.class);
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

        String hostKey = CacheKeyUtil.getHostConnectionKey(memGraphicsInfo.getHostId());
        HostContext hostContext = cacheService.get(hostKey);
        if (hostContext == null) {
            FunctionUtils.ignoreRun(webSocket::close);
            log.info("Vnc连接失败，宿主机不在线.guest={},hostId={}", memGraphicsInfo.getGuestName(), memGraphicsInfo.getHostId());
            return;
        }
        WsMessage<byte[]> msg = WsMessage.<byte[]>builder().command(Constant.SocketCommand.VNC_CONNECT).data(vncConnect.toBytes()).build();
        ActionDispatcher.dispatch(webSocket, msg);
    }


}
