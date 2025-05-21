package cn.chenjun.cloud.management.websocket.client.owner;

import cn.chenjun.cloud.management.util.Constant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class AgentWsOwner implements WsOwner {
    private String agentId;

    @Override
    public short getType() {
        return Constant.WsClientType.AGENT;
    }
}
