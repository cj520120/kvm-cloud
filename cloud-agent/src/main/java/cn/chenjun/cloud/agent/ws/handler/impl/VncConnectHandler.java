package cn.chenjun.cloud.agent.ws.handler.impl;

import cn.chenjun.cloud.agent.util.VncManager;
import cn.chenjun.cloud.agent.ws.client.WsClient;
import cn.chenjun.cloud.agent.ws.handler.PacketHandler;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.host.VncConnect;
import cn.chenjun.cloud.common.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class VncConnectHandler implements PacketHandler {

    @Override
    public void process(WsClient wsClient, WsMessage<byte[]> wsMessage) {
        String json = new String(wsMessage.getData(), StandardCharsets.UTF_8);
        VncConnect vnc = GsonBuilderUtil.create().fromJson(json, VncConnect.class);
        log.info("接收到新到Vnc连接请求:{}", vnc.getName());
        VncManager.connect(wsClient, vnc.getId(), vnc.getName());
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.VNC_CONNECT;
    }

}
