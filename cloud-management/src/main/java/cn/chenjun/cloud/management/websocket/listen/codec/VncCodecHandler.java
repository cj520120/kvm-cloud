package cn.chenjun.cloud.management.websocket.listen.codec;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.host.VncData;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.websocket.action.ActionDispatcher;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.listen.context.VncContext;

import javax.websocket.MessageHandler;
import java.nio.ByteBuffer;

public class VncCodecHandler implements MessageHandler.Whole<ByteBuffer> {

    private final Client webSocket;

    public VncCodecHandler(Client webSocket) {
        this.webSocket = webSocket;
    }

    @Override
    public void onMessage(ByteBuffer message) {
        VncContext context = (VncContext) webSocket.getContext();
        VncData vncData = new VncData();
        vncData.setData(message.array());
        vncData.setId(context.getId());
        WsMessage<byte[]> msg = WsMessage.<byte[]>builder().command(Constant.SocketCommand.VNC_DATA).data(vncData.toBytes()).build();
        ActionDispatcher.dispatch(webSocket, msg);
    }
}