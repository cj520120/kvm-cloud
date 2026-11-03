package cn.ruiyun.cloud.management.websocket.action.impl.global;

import cn.ruiyun.cloud.common.socket.packet.WsMessage;
import cn.ruiyun.cloud.common.socket.packet.data.host.VncDisconnect;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.websocket.action.WsAction;
import cn.ruiyun.cloud.management.websocket.listen.client.Client;
import cn.ruiyun.cloud.management.websocket.mgr.VncManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
public class VncDisconnectionAction implements WsAction<byte[]> {
    @Override
    public void doAction(Client webSocket, WsMessage<byte[]> msg) throws IOException {
        if (Objects.isNull(webSocket.getContext())) {
            webSocket.close();
            return;
        }
        VncDisconnect data = VncDisconnect.fromBytes(msg.getData());
        VncManager.disconnect(data);
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.VNC_DISCONNECT;
    }
}
