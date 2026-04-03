package cn.chenjun.cloud.management.websocket.listen.context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserContext implements ConnectContext {
    private int userId;
}
