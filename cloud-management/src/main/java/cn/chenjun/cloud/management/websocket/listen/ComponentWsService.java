package cn.chenjun.cloud.management.websocket.listen;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.websocket.action.ActionDispatcher;
import cn.chenjun.cloud.management.websocket.client.WebSocket;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.MessageHandler;
import javax.websocket.server.ServerEndpoint;

/**
 * @author chenjun
 */
@Slf4j
@Component
@ServerEndpoint(value = "/api/component/ws")
public class ComponentWsService extends AbstractWsService {

    @Override
    protected MessageHandler.Whole<String> createMessageHandler(WebSocket webSocket) {
        MessageHandler.Whole<String> handler = new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String json) {
                WsRequest msg = GsonBuilderUtil.create().fromJson(json, WsRequest.class);
                ActionDispatcher.dispatch(webSocket, msg);
            }
        };
        return handler;
    }
}
