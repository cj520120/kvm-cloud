package cn.chenjun.cloud.management.websocket.listen;

import cn.chenjun.cloud.common.event.EventHandler;
import cn.chenjun.cloud.common.event.EventObject;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.config.ApplicationConfig;
import cn.chenjun.cloud.management.data.entity.ComponentGuestEntity;
import cn.chenjun.cloud.management.servcie.ComponentService;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.util.SpringContextUtils;
import cn.chenjun.cloud.management.websocket.action.ActionDispatcher;
import cn.chenjun.cloud.management.websocket.client.WebSocket;
import cn.chenjun.cloud.management.websocket.client.context.ComponentContext;
import cn.chenjun.cloud.management.websocket.client.context.WebsocketContext;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.websocket.MessageHandler;
import javax.websocket.server.ServerEndpoint;
import java.util.Date;
import java.util.Objects;

/**
 * @author chenjun
 */
@Slf4j
@Component
@ServerEndpoint(value = "/api/component/ws")
public class ComponentWsService extends AbstractWsService {
    @Autowired
    private ApplicationConfig applicationConfig;
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

    @Override
    protected void onConnection(WebSocket webSocket) {
        ComponentService componentService = SpringContextUtils.getBean(ComponentService.class);
        LockRunner lockRunner = SpringContextUtils.getBean(LockRunner.class);
        webSocket.onClose.addEvent(new EventHandler<WebsocketContext>() {
            @Transactional(rollbackFor = Exception.class)
            @Override
            public void fire(Object sender, EventObject<WebsocketContext> obj) {
                ComponentContext context = (ComponentContext) obj.getEvent();
                if (context != null) {
                    //设置网络组件下线
                    lockRunner.lockCall(RedisKeyUtil.getGlobalLockKey(), () -> {

                        ComponentGuestEntity componentGuest = componentService.findComponentGuestById(context.getComponentGuestId());
                        if (componentGuest != null && Objects.equals(componentGuest.getSessionId(), context.getSessionId())) {
                            componentGuest.setLastActiveTime(new Date());
                            componentGuest.setStatus(Constant.ComponentGuestStatus.OFFLINE);
                            componentService.updateComponentGuest(componentGuest);
                            log.info("component guest offline {}", componentGuest);
                        }
                        return null;
                    });
                }
            }
        });
    }
}
