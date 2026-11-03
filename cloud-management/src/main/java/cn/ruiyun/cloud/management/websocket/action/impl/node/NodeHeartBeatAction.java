package cn.ruiyun.cloud.management.websocket.action.impl.node;

import cn.ruiyun.cloud.common.socket.packet.WsMessage;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.websocket.action.WsAction;
import cn.ruiyun.cloud.management.websocket.listen.client.Client;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NodeHeartBeatAction implements WsAction<byte[]> {


    @Override
    public void doAction(Client webSocket, WsMessage<byte[]> msg) throws IOException {

    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.NODE_HEART_BEAT;
    }
}
