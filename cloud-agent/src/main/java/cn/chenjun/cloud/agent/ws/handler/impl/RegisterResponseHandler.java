package cn.chenjun.cloud.agent.ws.handler.impl;

import cn.chenjun.cloud.agent.ws.client.WsClient;
import cn.chenjun.cloud.agent.ws.handler.PacketHandler;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.host.RegisterResponse;
import cn.chenjun.cloud.common.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class RegisterResponseHandler implements PacketHandler {
    @Override
    public void process(WsClient wsClient, WsMessage<byte[]> wsMessage) {
        String json = new String(wsMessage.getData(), StandardCharsets.UTF_8);
        RegisterResponse registerResponse = GsonBuilderUtil.create().fromJson(json, RegisterResponse.class);
        if (registerResponse.isSuccess()) {
            log.info("注册成功");
            wsClient.login();
        } else {
            log.error("注册失败,关闭连接:{}", registerResponse.getMessage());
            wsClient.close();
        }
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.AGENT_REGISTER_RESPONSE;
    }

}
