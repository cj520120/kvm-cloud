package cn.chenjun.cloud.management.websocket.client.context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComponentContext implements WebsocketContext {
    private int networkId;
    private int componentId;
}
