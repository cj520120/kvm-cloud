package cn.chenjun.cloud.agent.ws.handler;

import cn.chenjun.cloud.agent.ws.client.WsClient;
import cn.chenjun.cloud.common.socket.packet.WsMessage;

public interface PacketHandler {

    void process(WsClient wsClient, WsMessage<byte[]> wsMessage);

    int getCommand();
}
