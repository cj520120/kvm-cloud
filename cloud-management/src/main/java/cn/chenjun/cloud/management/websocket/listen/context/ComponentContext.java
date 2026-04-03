package cn.chenjun.cloud.management.websocket.listen.context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComponentContext implements ConnectContext {
    private int networkId;
    private int guestId;
    private int componentId;
    private int componentGuestId;
    private int status;
    private String sessionId;

}
