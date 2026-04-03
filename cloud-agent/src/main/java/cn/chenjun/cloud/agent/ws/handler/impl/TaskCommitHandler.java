package cn.chenjun.cloud.agent.ws.handler.impl;

import cn.chenjun.cloud.agent.operate.OperateDispatch;
import cn.chenjun.cloud.agent.ws.client.WsClient;
import cn.chenjun.cloud.agent.ws.handler.PacketHandler;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class TaskCommitHandler implements PacketHandler {
    @Autowired
    private OperateDispatch dispatch;

    @Override
    public void process(WsClient wsClient, WsMessage<byte[]> wsMessage) {
        String json = new String(wsMessage.getData(), StandardCharsets.UTF_8);
        dispatch.dispatch(json);
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.AGENT_TASK_SUBMIT;
    }
}
