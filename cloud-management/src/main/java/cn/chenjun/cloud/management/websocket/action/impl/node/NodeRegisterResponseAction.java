package cn.chenjun.cloud.management.websocket.action.impl.node;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.host.RegisterResponse;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.config.ApplicationConfig;
import cn.chenjun.cloud.management.servcie.HostService;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.listen.context.NodeContext;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class NodeRegisterResponseAction implements WsAction<byte[]> {
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
        RegisterResponse data = RegisterResponse.fromBytes(msg.getData());
        if (data.isSuccess()) {
            webSocket.login(new NodeContext());
        } else {
            webSocket.close();
        }

    }


    @Override
    public int getCommand() {
        return Constant.SocketCommand.NODE_REGISTER_RESPONSE;
    }

}
