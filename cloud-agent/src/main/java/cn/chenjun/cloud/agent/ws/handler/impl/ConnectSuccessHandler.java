package cn.chenjun.cloud.agent.ws.handler.impl;

import cn.chenjun.cloud.agent.util.ClientService;
import cn.chenjun.cloud.agent.ws.client.WsClient;
import cn.chenjun.cloud.agent.ws.handler.PacketHandler;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ConnectSuccessHandler implements PacketHandler {
    @Autowired
    private ClientService clientService;

    @Override
    public void process(WsClient wsClient, WsMessage<byte[]> wsMessage) {
        Map<String, Object> map = new HashMap<>();
        map.put("clientId", clientService.getClientId());
        map.put("nonce", UUID.randomUUID().toString().replace("-", ""));
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        map.put("sign", SecurityUtil.signature(map, clientService.getClientSecret()));
        wsClient.sendJson(Constant.SocketCommand.AGENT_REGISTER, map);
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.AGENT_CONNECT;
    }
}
