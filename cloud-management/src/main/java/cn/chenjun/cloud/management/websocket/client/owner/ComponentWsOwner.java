package cn.chenjun.cloud.management.websocket.client.owner;

import cn.chenjun.cloud.management.util.Constant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class ComponentWsOwner implements WsOwner {
    private int networkId;
    private int componentId;

    @Override
    public short getType() {
        return Constant.WsClientType.COMPONENT;
    }
}
