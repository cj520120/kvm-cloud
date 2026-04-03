package cn.chenjun.cloud.management.websocket.action.impl.agent;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.FunctionUtils;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.servcie.HostService;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.listen.context.HostContext;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class AgentHeartBeatAction implements WsAction<byte[]> {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private HostService hostService;
    @Autowired
    private LockRunner lockRunner;


    @Override
    public void doAction(Client webSocket, WsMessage<byte[]> msg) throws IOException {
        HostContext context = (HostContext) webSocket.getContext();
        if (Objects.isNull(context)) {
            return;
        }
        HostEntity host = FunctionUtils.ignoreErrorCall(() -> hostService.getHostById(context.getHostId()));
        if (Objects.isNull(host)) {
            return;
        }
        String key = RedisKeyUtil.getHostConnectionKey(host.getHostId());
        redissonClient.getBucket(key).set(context, 30, TimeUnit.SECONDS);
        if (host.getStatus().equals(Constant.HostStatus.OFFLINE)) {
            lockRunner.lockCall(RedisKeyUtil.getGlobalLockKey(), () -> hostService.registerHost(context.getHostId()));
        }
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.AGENT_HEART_BEAT;
    }
}
