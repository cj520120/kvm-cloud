package cn.ruiyun.cloud.agent.ws.handler.impl;

import cn.ruiyun.cloud.agent.ws.client.WsClient;
import cn.ruiyun.cloud.agent.ws.handler.PacketHandler;
import cn.ruiyun.cloud.common.socket.packet.WsMessage;
import cn.ruiyun.cloud.common.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PingHandler implements PacketHandler {

    @Override
    public void process(WsClient wsClient, WsMessage<byte[]> wsMessage) {
        wsClient.sendCommand(Constant.SocketCommand.PONG);
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.PING;
    }

}
