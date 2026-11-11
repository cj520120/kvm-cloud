package cn.ruiyun.cloud.management.websocket.listen.server;

import cn.ruiyun.cloud.common.event.EventObject;
import cn.ruiyun.cloud.common.socket.packet.WsMessage;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.data.entity.HostEntity;
import cn.ruiyun.cloud.management.servcie.CacheService;
import cn.ruiyun.cloud.management.servcie.HostService;
import cn.ruiyun.cloud.management.servcie.LockRunner;
import cn.ruiyun.cloud.management.util.CacheKeyUtil;
import cn.ruiyun.cloud.management.util.NotifyContextHolderUtil;
import cn.ruiyun.cloud.management.util.SpringContextUtils;
import cn.ruiyun.cloud.management.websocket.common.SocketType;
import cn.ruiyun.cloud.management.websocket.listen.client.BinarySocket;
import cn.ruiyun.cloud.management.websocket.listen.client.Client;
import cn.ruiyun.cloud.management.websocket.listen.codec.ByteCodecHandler;
import cn.ruiyun.cloud.management.websocket.listen.context.ConnectContext;
import cn.ruiyun.cloud.management.websocket.listen.context.HostContext;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @author chenjun
 */
@Slf4j
@Component
@ServerEndpoint(value = "/api/host/ws")
public class AgentListen extends AbstractWsService<WsMessage<byte[]>, ByteBuffer> {
    public AgentListen() {
        super(ByteCodecHandler::new);
    }


    @Override
    protected Client createWebSocket(Session session) {
        return new BinarySocket(session, SocketType.AGENT_SOCKET);
    }

    @Override
    protected void onConnection(Client webSocket) {
        super.onConnection(webSocket);
        webSocket.registerOnClose(this::onClientCloseHandler);
        webSocket.sendBinaryPacket(Constant.SocketCommand.AGENT_CONNECT, null);
    }

    @Override
    protected int getSocketType() {
        return SocketType.AGENT_SOCKET;
    }

    @Override
    protected int getTimeoutSeconds() {
        return 30;
    }

    public void onClientCloseHandler(Object sender, EventObject<ConnectContext> obj) {
        HostContext context = (HostContext) obj.getEvent();
        HostService hostService = SpringContextUtils.getBean(HostService.class);
        CacheService cacheService = SpringContextUtils.getBean(CacheService.class);
        LockRunner lockRunner = SpringContextUtils.getBean(LockRunner.class);
        if (context != null) {
            lockRunner.lockCall(CacheKeyUtil.getGlobalLockKey(), () -> {
                String key = CacheKeyUtil.getHostConnectionKey(context.getHostId());
                HostContext sessionContext = cacheService.get(key);
                if (Objects.nonNull(sessionContext) && Objects.equals(sessionContext.getSessionId(), context.getSessionId())) {
                    HostEntity host = hostService.getHostById(context.getHostId());
                    if (!Objects.equals(host.getStatus(), Constant.HostStatus.MAINTENANCE)) {
                        host.setStatus(Constant.HostStatus.OFFLINE);
                        hostService.updateHost(host);
                        cacheService.delete(key);
                        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(host.getHostId()).type(Constant.NotifyType.UPDATE_HOST).build());
                    }
                }
                return null;
            });
        }
    }
}
