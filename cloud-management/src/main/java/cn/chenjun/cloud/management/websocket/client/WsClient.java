package cn.chenjun.cloud.management.websocket.client;

import cn.chenjun.cloud.management.websocket.client.owner.WsOwner;
import lombok.Builder;
import lombok.Data;

import javax.websocket.Session;

/**
 * @author chenjun
 */
@Data
@Builder
public class WsClient<T extends WsOwner> {
    private Session session;

    private T owner;


}
