package cn.ruiyun.cloud.management.websocket.listen.server;


import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.event.EventHandler;
import cn.ruiyun.cloud.common.gson.GsonBuilderUtil;
import cn.ruiyun.cloud.common.socket.packet.WsMessage;
import cn.ruiyun.cloud.common.util.AppUtils;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.common.util.FunctionUtils;
import cn.ruiyun.cloud.common.util.MapUtil;
import cn.ruiyun.cloud.management.data.dao.HostDao;
import cn.ruiyun.cloud.management.data.entity.HostEntity;
import cn.ruiyun.cloud.management.servcie.CacheService;
import cn.ruiyun.cloud.management.servcie.bean.MemGraphicsInfo;
import cn.ruiyun.cloud.management.util.CacheKeyUtil;
import cn.ruiyun.cloud.management.util.SpringContextUtils;
import cn.ruiyun.cloud.management.websocket.common.SocketType;
import cn.ruiyun.cloud.management.websocket.listen.client.AgentVncSocket;
import cn.ruiyun.cloud.management.websocket.listen.client.Client;
import cn.ruiyun.cloud.management.websocket.listen.client.VncSocket;
import cn.ruiyun.cloud.management.websocket.listen.codec.DirectVncCodecHandler;
import cn.ruiyun.cloud.management.websocket.listen.context.ConnectContext;
import cn.ruiyun.cloud.management.websocket.listen.context.VncContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
@ServerEndpoint(value = "/api/vnc")
@Component
public class VncListen extends AbstractWsService<WsMessage<byte[]>, ByteBuffer> {
    public VncListen() {
        super(DirectVncCodecHandler::new);
    }

    private static String buildAgentVncUrl(String agentUrl) {
        if (agentUrl == null) {
            return "";
        }
        String normalized = agentUrl.trim()
                .replaceFirst("(?i)^http://", "ws://")
                .replaceFirst("(?i)^https://", "wss://");
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized + "/api/vnc";
    }

    @Override
    protected int getTimeoutSeconds() {
        return 30;
    }

    @Override
    protected int getSocketType() {
        return SocketType.VNC_SOCKET;
    }

    @Override
    protected Client createWebSocket(Session session) {
        VncSocket webSocket = new VncSocket(session);
        List<String> queryValues = session.getRequestParameterMap().get("token");
        String token = queryValues == null || queryValues.isEmpty() ? null : queryValues.get(0);
        CacheService cacheService = SpringContextUtils.getBean(CacheService.class);
        MemGraphicsInfo memGraphicsInfo = cacheService.getAndRemove(CacheKeyUtil.getGuestGraphicsToken(token));
        if (Objects.isNull(memGraphicsInfo)) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "token无效");
        }
        webSocket.putAttribute("vnc", memGraphicsInfo);
        return webSocket;
    }

    @SneakyThrows
    @Override
    protected void onConnection(Client webSocket) {
        super.onConnection(webSocket);
        MemGraphicsInfo memGraphicsInfo = (MemGraphicsInfo) webSocket.getAttribute("vnc");
        if (memGraphicsInfo.getHostId() <= 0) {
            webSocket.close();
            log.info("Vnc连接失败，虚拟机未运行或不存在.guest={}", memGraphicsInfo.getGuestName());
            return;
        }
        String sessionId = UUID.randomUUID().toString();
        VncContext vncContext = VncContext.builder().id(sessionId).build();
        webSocket.login(vncContext);

        HostEntity host = SpringContextUtils.getBean(HostDao.class).findById(memGraphicsInfo.getHostId());
        if (host == null || StringUtils.isAnyBlank(host.getUri(), host.getClientId(), host.getClientSecret())) {
            FunctionUtils.ignoreRun(webSocket::close);
            log.error("VNC直连失败，宿主机连接信息不完整,guestId={},guestName={},hostId={}",
                    memGraphicsInfo.getGuestId(), memGraphicsInfo.getGuestName(), memGraphicsInfo.getHostId());
            return;
        }

        String agentUrl = buildAgentVncUrl(host.getUri());
        if (!agentUrl.startsWith("ws://") && !agentUrl.startsWith("wss://")) {
            FunctionUtils.ignoreRun(webSocket::close);
            log.error("VNC直连失败，宿主机通信地址无效,guestId={},guestName={},hostId={},uri={}",
                    memGraphicsInfo.getGuestId(), memGraphicsInfo.getGuestName(), memGraphicsInfo.getHostId(), host.getUri());
            return;
        }
        URI url = new URI(agentUrl);

        Map<String, Object> connectData = new HashMap<>();
        connectData.put("name", memGraphicsInfo.getGuestName());
        // 签名参数必须使用字符串。JSON 反序列化到 Map 后，数字会变成 Double，
        // 导致 200322 与 200322.0 的签名原文不一致。
        connectData.put("guestId", String.valueOf(memGraphicsInfo.getGuestId()));
        connectData.put("hostId", String.valueOf(memGraphicsInfo.getHostId()));
        connectData.put("timestamp", String.valueOf(System.currentTimeMillis()));
        String nonce = UUID.randomUUID().toString();
        connectData.put("sign", AppUtils.sign(connectData, host.getClientId(), host.getClientSecret(), nonce));
        Map<String, String> headers = MapUtil.of("x-data", GsonBuilderUtil.create().toJson(connectData));
        AgentVncSocket agentVncSocket = new AgentVncSocket(url, headers, (VncSocket) webSocket, sessionId,
                memGraphicsInfo.getGuestId(), memGraphicsInfo.getGuestName(), memGraphicsInfo.getHostId());
        webSocket.putAttribute(AgentVncSocket.ATTRIBUTE_NAME, agentVncSocket);
        try {
            EventHandler<ConnectContext> onCloseHandler = (sender, obj) -> {
                FunctionUtils.ignoreRun(agentVncSocket::close);
            };
            webSocket.registerOnClose(onCloseHandler);

            if (!agentVncSocket.connectBlocking(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("连接宿主机Agent超时: " + url);
            }
            log.info("VNC由管理端直连宿主机Agent,guest={},hostId={},target={}",
                    memGraphicsInfo.getGuestName(), memGraphicsInfo.getHostId(), url);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("VNC直连宿主机Agent失败,guestId={},guestName={},hostId={},sessionId={},target={}",
                    memGraphicsInfo.getGuestId(), memGraphicsInfo.getGuestName(), memGraphicsInfo.getHostId(),
                    sessionId, url, e);
            FunctionUtils.ignoreRun(webSocket::close);
            FunctionUtils.ignoreRun(agentVncSocket::close);
        }
    }


}
