package cn.chenjun.cloud.management.websocket.action.impl.node;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.base.MapData;
import cn.chenjun.cloud.common.socket.packet.data.host.RegisterResponse;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.SecurityUtil;
import cn.chenjun.cloud.management.config.ApplicationConfig;
import cn.chenjun.cloud.management.servcie.HostService;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.listen.context.NodeContext;
import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class NodeRegisterAction implements WsAction<byte[]> {
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
            long timestamp = NumberUtil.parseLong(params.getOrDefault("timestamp", "0").toString());
            long expire = timestamp + TimeUnit.SECONDS.toMillis(30);
            if (expire < System.currentTimeMillis()) {
                return RegisterResponse.error("签名时间验证失败,请确认服务器时间是否同步");
            }
            String sign = params.remove("sign").toString();
            if (!SecurityUtil.signature(params, applicationConfig.getCluster().getToken()).equals(sign)) {
                return RegisterResponse.error("无效的签名数据");
            }
            webSocket.login(new NodeContext());
            log.info("Node节点注册成功,id={}", webSocket.getSessionId());
            return RegisterResponse.builder().success(true).build();
        });
        webSocket.sendBinaryPacket(Constant.SocketCommand.NODE_REGISTER_RESPONSE, response.toBytes());

    }


    @Override
    public int getCommand() {
        return Constant.SocketCommand.NODE_REGISTER;
    }

}
