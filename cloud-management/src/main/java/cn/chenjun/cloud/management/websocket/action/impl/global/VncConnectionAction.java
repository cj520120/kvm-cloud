package cn.chenjun.cloud.management.websocket.action.impl.global;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.host.VncConnect;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.mgr.VncManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
public class VncConnectionAction implements WsAction<byte[]> {
    @Override
    public void doAction(Client webSocket, WsMessage<byte[]> msg) throws IOException {
        if (Objects.isNull(webSocket.getContext())) {
            webSocket.close();
            return;
        }
        VncConnect vncConnect = VncConnect.fromBytes(msg.getData());
        log.info("Vnc连接请求,id={},hostId={}", webSocket.getSessionId(), vncConnect.getHostId());
        VncManager.connect(webSocket, vncConnect);
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.VNC_CONNECT;
    }
}
