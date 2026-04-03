package cn.chenjun.cloud.agent.ws.handler.impl;

import cn.chenjun.cloud.agent.util.VncManager;
import cn.chenjun.cloud.agent.ws.client.WsClient;
import cn.chenjun.cloud.agent.ws.handler.PacketHandler;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VncDataHandler implements PacketHandler {

    @Override
    public void process(WsClient wsClient, WsMessage<byte[]> wsMessage) {
        VncManager.forward(wsClient, wsMessage.getData());
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.VNC_DATA;
    }


}
