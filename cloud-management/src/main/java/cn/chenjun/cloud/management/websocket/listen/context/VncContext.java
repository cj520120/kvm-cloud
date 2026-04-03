package cn.chenjun.cloud.management.websocket.listen.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VncContext implements ConnectContext {
    private String id;
}
