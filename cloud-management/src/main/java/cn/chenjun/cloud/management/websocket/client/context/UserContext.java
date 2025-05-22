package cn.chenjun.cloud.management.websocket.client.context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserContext implements WebsocketContext {
    private int userId;
}
