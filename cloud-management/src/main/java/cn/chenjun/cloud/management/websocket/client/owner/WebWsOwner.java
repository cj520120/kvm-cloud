package cn.chenjun.cloud.management.websocket.client.owner;

import cn.chenjun.cloud.management.util.Constant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class WebWsOwner implements WsOwner {
    private int userId;

    @Override
    public short getType() {
        return Constant.WsClientType.WEB;
    }
}
