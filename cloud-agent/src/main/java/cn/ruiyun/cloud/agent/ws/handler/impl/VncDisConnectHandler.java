package cn.ruiyun.cloud.agent.ws.handler.impl;

import cn.ruiyun.cloud.agent.util.VncManager;
import cn.ruiyun.cloud.agent.ws.client.WsClient;
import cn.ruiyun.cloud.agent.ws.handler.PacketHandler;
import cn.ruiyun.cloud.common.gson.GsonBuilderUtil;
import cn.ruiyun.cloud.common.socket.packet.WsMessage;
import cn.ruiyun.cloud.common.socket.packet.data.host.VncDisconnect;
import cn.ruiyun.cloud.common.util.Constant;
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
