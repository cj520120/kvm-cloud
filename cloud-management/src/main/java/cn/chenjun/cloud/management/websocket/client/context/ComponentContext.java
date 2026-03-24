package cn.chenjun.cloud.management.websocket.client.context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComponentContext implements WebsocketContext {
    private int networkId;
    private int guestId;
    private int componentId;
    private int componentGuestId;
    private int status;
    private String sessionId;

}
