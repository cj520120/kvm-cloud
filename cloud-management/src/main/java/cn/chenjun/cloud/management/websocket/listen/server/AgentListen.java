package cn.chenjun.cloud.management.websocket.listen.server;

import cn.chenjun.cloud.common.event.EventObject;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.servcie.HostService;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.util.SpringContextUtils;
import cn.chenjun.cloud.management.websocket.common.SocketType;
import cn.chenjun.cloud.management.websocket.listen.client.BinarySocket;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.listen.codec.ByteCodecHandler;
import cn.chenjun.cloud.management.websocket.listen.context.ConnectContext;
import cn.chenjun.cloud.management.websocket.listen.context.HostContext;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Objects;

/**
 * @author chenjun
 */
@Slf4j
@Component
@ServerEndpoint(value = "/api/host/ws")
public class AgentListen extends AbstractWsService {
    public AgentListen() {
        super((client) -> new ByteCodecHandler(client));
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
        RedissonClient redissonClient = SpringContextUtils.getBean(RedissonClient.class);
        LockRunner lockRunner = SpringContextUtils.getBean(LockRunner.class);
        if (context != null) {
            lockRunner.lockCall(RedisKeyUtil.getGlobalLockKey(), () -> {
                String key = RedisKeyUtil.getHostConnectionKey(context.getHostId());
                RBucket<HostContext> bucket = redissonClient.getBucket(key);
                HostContext sessionContext = bucket.get();
                if (Objects.nonNull(sessionContext) && Objects.equals(sessionContext.getSessionId(), context.getSessionId())) {
                    HostEntity host = hostService.getHostById(context.getHostId());
                    if (!Objects.equals(host.getStatus(), Constant.HostStatus.MAINTENANCE)) {
                        host.setStatus(Constant.HostStatus.OFFLINE);
                        hostService.updateHost(host);
                        bucket.delete();
                        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(host.getHostId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_HOST).build());
                    }
                }
                return null;
            });
        }
    }
}
