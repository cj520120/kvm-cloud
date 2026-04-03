package cn.chenjun.cloud.common.socket.packet.data.host;

import cn.chenjun.cloud.common.socket.packet.data.BaseJsonData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VncDisconnect extends BaseJsonData {
    private String id;

    public static VncDisconnect fromBytes(byte[] bytes) {
        return fromBytes(bytes, VncDisconnect.class);
    }

    public static VncDisconnect fromBuffer(ByteBuffer byteBuffer) {
        return fromBytes(byteBuffer.array(), VncDisconnect.class);
    }
}
