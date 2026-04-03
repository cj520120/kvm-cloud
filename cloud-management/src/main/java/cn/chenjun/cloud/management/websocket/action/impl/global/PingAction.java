package cn.chenjun.cloud.management.websocket.action.impl.global;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class PingAction implements WsAction<Object> {
    @Override
    public void doAction(Client webSocket, WsMessage<Object> msg) throws IOException {
        webSocket.sendCommand(Constant.SocketCommand.PONG);
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.PING;
    }
}
