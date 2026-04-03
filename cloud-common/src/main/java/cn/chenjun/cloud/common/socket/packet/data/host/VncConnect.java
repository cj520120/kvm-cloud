package cn.chenjun.cloud.common.socket.packet.data.host;

import cn.chenjun.cloud.common.socket.packet.data.BaseJsonData;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class VncConnect extends BaseJsonData {
    private String name;
    private String id;
    private int hostId;

    public static VncConnect fromBytes(byte[] bytes) {
        return fromBytes(bytes, VncConnect.class);
    }

    public static VncConnect fromBuffer(ByteBuffer byteBuffer) {
        return fromBuffer(byteBuffer, VncConnect.class);
    }
}
