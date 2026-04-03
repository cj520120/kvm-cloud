package cn.chenjun.cloud.management.websocket.listen.codec;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.common.util.FunctionUtils;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
public class ByteCodecHandler extends BaseCodecHandler<WsMessage<byte[]>, ByteBuffer> {
    public ByteCodecHandler(Client webSocket) {
        super(webSocket);
    }

    @Override
    protected WsMessage<byte[]> decode(ByteBuffer buffer) {
        try {
            int command = buffer.getInt();
            int dataLength = buffer.getInt();
            byte[] dataBuffer = new byte[dataLength];
            if (dataLength > 0) {
                buffer.get(dataBuffer, 0, dataLength);
            }
            WsMessage<byte[]> msg = new WsMessage<>();
            msg.setCommand(command);
            msg.setData(dataBuffer);
            return msg;
        } catch (Exception e) {
            FunctionUtils.ignoreRun(webSocket::close);
            log.error("解码失败", e);
            throw new CodeException(ErrorCode.SERVER_ERROR, "解码失败");
        }
    }
}
