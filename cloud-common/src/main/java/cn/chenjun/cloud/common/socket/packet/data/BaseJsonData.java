package cn.chenjun.cloud.common.socket.packet.data;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class BaseJsonData implements BaseData {
    public static <T extends BaseJsonData> T fromBytes(byte[] bytes, Class<T> clazz) {
        return GsonBuilderUtil.create().fromJson(new String(bytes, StandardCharsets.UTF_8), clazz);
    }

    public static <T extends BaseJsonData> T fromBuffer(ByteBuffer byteBuffer, Class<T> clazz) {
        return fromBytes(byteBuffer.array(), clazz);
    }

    @Override
    public byte[] toBytes() {
        String json = GsonBuilderUtil.create().toJson(this);
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
