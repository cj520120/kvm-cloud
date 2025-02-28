package cn.chenjun.cloud.management.websocket.client;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import lombok.Builder;
import lombok.Data;

import javax.websocket.Session;
import java.io.IOException;

/**
 * @author chenjun
 */
@Data
@Builder
public class WsClient {
    private Session session;

    private int networkId;
    private int componentId;

    private short clientType;

    public <T> void send(WsMessage<T> msg) throws IOException {
        session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(msg));
    }
}
