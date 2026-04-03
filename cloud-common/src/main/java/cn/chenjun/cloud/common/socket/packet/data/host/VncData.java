package cn.chenjun.cloud.common.socket.packet.data.host;

import cn.chenjun.cloud.common.socket.packet.data.BaseData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class VncData implements BaseData {
    private String id;
    private byte[] data;

    public static VncData fromBytes(byte[] bytes) {
        return fromBuffer(ByteBuffer.wrap(bytes));
    }

    public static VncData fromBuffer(ByteBuffer byteBuffer) {
        int idLength = byteBuffer.getInt();
        byte[] idBytes = new byte[idLength];
        byteBuffer.get(idBytes);
        String id = new String(idBytes, StandardCharsets.UTF_8);
        int dataLength = byteBuffer.getInt();
        byte[] data = new byte[dataLength];
        byteBuffer.get(data);
        VncData vncData = new VncData();
        vncData.setId(id);
        vncData.setData(data);
        return vncData;
    }

    @Override
    public byte[] toBytes() {
        byte[] idBytes = id.getBytes(StandardCharsets.UTF_8);
        int idLength = idBytes.length;
        int dataLength = data.length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(8 + idLength + dataLength);
        byteBuffer.putInt(idLength);
        byteBuffer.put(idBytes);
        byteBuffer.putInt(dataLength);
        byteBuffer.put(this.data);
        byteBuffer.flip();
        return byteBuffer.array();
    }
}
