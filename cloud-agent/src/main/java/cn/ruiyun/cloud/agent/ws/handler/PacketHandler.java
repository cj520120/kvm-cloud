package cn.ruiyun.cloud.agent.ws.handler;

import cn.ruiyun.cloud.agent.ws.client.WsClient;
import cn.ruiyun.cloud.common.socket.packet.WsMessage;

public interface PacketHandler {

    void process(WsClient wsClient, WsMessage<byte[]> wsMessage);

    int getCommand();
}
