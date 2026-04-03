package cn.chenjun.cloud.agent.ws.handler.impl;

import cn.chenjun.cloud.agent.util.VncManager;
import cn.chenjun.cloud.agent.ws.client.WsClient;
import cn.chenjun.cloud.agent.ws.handler.PacketHandler;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.host.VncDisconnect;
import cn.chenjun.cloud.common.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class VncDisConnectHandler implements PacketHandler {

    @Override
    public void process(WsClient wsClient, WsMessage<byte[]> wsMessage) {
        String json = new String(wsMessage.getData(), StandardCharsets.UTF_8);
        VncDisconnect vnc = GsonBuilderUtil.create().fromJson(json, VncDisconnect.class);
        VncManager.disconnect(vnc.getId());
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.VNC_DISCONNECT;
    }

}
