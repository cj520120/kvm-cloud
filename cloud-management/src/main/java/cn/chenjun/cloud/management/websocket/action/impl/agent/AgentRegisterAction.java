package cn.chenjun.cloud.management.websocket.action.impl.agent;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.base.MapData;
import cn.chenjun.cloud.common.socket.packet.data.host.RegisterResponse;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.FunctionUtils;
import cn.chenjun.cloud.common.util.SecurityUtil;
import cn.chenjun.cloud.management.config.ApplicationConfig;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.servcie.HostService;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.listen.context.HostContext;
import cn.chenjun.cloud.management.websocket.manager.HostClientManager;
import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class AgentRegisterAction implements WsAction<byte[]> {
    @Autowired
    private HostService hostService;
    @Autowired
    private LockRunner lockRunner;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ApplicationConfig applicationConfig;

    @Override
    public void doAction(Client webSocket, WsMessage<byte[]> msg) throws IOException {
        MapData params = MapData.fromBytes(msg.getData());
        RegisterResponse response = lockRunner.lockCall(RedisKeyUtil.getGlobalLockKey(), () -> {
            String clientId = (String) params.get("clientId");
            String sign = params.remove("sign").toString();
            if (Objects.isNull(sign)) {
                return RegisterResponse.error("签名不能为空");
            }

            long timestamp = NumberUtil.parseLong(params.getOrDefault("timestamp", "0").toString());
            long expire = timestamp + TimeUnit.SECONDS.toMillis(30);
            if (expire < System.currentTimeMillis()) {
                return RegisterResponse.error("签名时间验证失败,请确认服务器时间是否同步");
            }
            HostEntity host = FunctionUtils.ignoreErrorCall(() -> hostService.getHostByClientId(clientId));
            if (Objects.isNull(host)) {
                return RegisterResponse.error("客户端不存在");
            }
            if (!SecurityUtil.signature(params, host.getClientSecret()).equals(sign)) {
                return RegisterResponse.error("无效的签名数据");
            }
            String sessionId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            HostContext context = HostContext.builder().hostId(host.getHostId()).nodeUrl(this.applicationConfig.getCluster().getNodeUrl()).sessionId(sessionId).build();
            String key = RedisKeyUtil.getHostConnectionKey(host.getHostId());
            redissonClient.getBucket(key).set(context, 30, TimeUnit.SECONDS);
            webSocket.login(context);
            HostClientManager.addHost(context.getHostId(), webSocket);
            if (!Objects.equals(host.getStatus(), Constant.HostStatus.MAINTENANCE)) {
                hostService.registerHost(host.getHostId());
            }
            log.info("host {} connect success", host);
            return RegisterResponse.builder().success(true).build();
        });
        webSocket.sendBinaryPacket(Constant.SocketCommand.AGENT_REGISTER_RESPONSE, response.toBytes());

    }


    @Override
    public int getCommand() {
        return Constant.SocketCommand.AGENT_REGISTER;
    }

}
