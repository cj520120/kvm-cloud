package cn.chenjun.cloud.common.socket.packet.data.base;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.socket.packet.data.BaseData;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class MapData extends HashMap<String, Object> implements BaseData {
    public static MapData fromBytes(byte[] bytes) {
        return GsonBuilderUtil.create().fromJson(new String(bytes, StandardCharsets.UTF_8), MapData.class);
    }

    public static MapData fromBuffer(ByteBuffer byteBuffer) {
        return fromBytes(byteBuffer.array());
    }

    @Override
    public byte[] toBytes() {
        String json = GsonBuilderUtil.create().toJson(this);
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
