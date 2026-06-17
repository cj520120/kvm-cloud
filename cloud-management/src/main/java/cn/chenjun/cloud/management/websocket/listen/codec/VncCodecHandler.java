package cn.chenjun.cloud.management.websocket.listen.codec;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.host.VncData;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.websocket.action.ActionDispatcher;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.listen.context.VncContext;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VncCodecHandler extends BaseCodecHandler<WsMessage<byte[]>, ByteBuffer> {
    private final ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream();
    private final Client webSocket;

    public VncCodecHandler(Client webSocket) {
        this.webSocket = webSocket;
    }


    @Override
    public void close() throws IOException {
        messageBuffer.close();
    }

    @SneakyThrows
    @Override
    public void onMessage(ByteBuffer messagePart, boolean last) {
        messageBuffer.write(messagePart.array());
        if (last) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(messageBuffer.toByteArray());
            VncContext context = (VncContext) webSocket.getContext();
            VncData vncData = new VncData();
            vncData.setData(byteBuffer.array());
            vncData.setId(context.getId());
            WsMessage<byte[]> msg = WsMessage.<byte[]>builder().command(Constant.SocketCommand.VNC_DATA).data(vncData.toBytes()).build();
            ActionDispatcher.dispatch(webSocket, msg);
            messageBuffer.reset();
        }

    }
}