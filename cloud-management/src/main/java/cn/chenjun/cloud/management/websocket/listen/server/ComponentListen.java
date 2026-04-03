package cn.chenjun.cloud.management.websocket.listen.server;

import cn.chenjun.cloud.common.event.EventObject;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.base.MapData;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.ComponentGuestEntity;
import cn.chenjun.cloud.management.servcie.ComponentService;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.util.SpringContextUtils;
import cn.chenjun.cloud.management.websocket.common.SocketType;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.listen.client.JsonSocket;
import cn.chenjun.cloud.management.websocket.listen.codec.JsonCodecHandler;
import cn.chenjun.cloud.management.websocket.listen.context.ComponentContext;
import cn.chenjun.cloud.management.websocket.listen.context.ConnectContext;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Date;
import java.util.Objects;

/**
 * @author chenjun
 */
@Slf4j
@Component
@ServerEndpoint(value = "/api/component/ws")
public class ComponentListen extends AbstractWsService {
    public ComponentListen() {
        super((client) -> new JsonCodecHandler<WsMessage<MapData>>(client, new TypeToken<WsMessage<MapData>>() {
        }.getType()));
    }

    @Override
    protected Client createWebSocket(Session session) {
        return new JsonSocket(session, SocketType.COMPONENT_SOCKET);
    }

    @Override
    protected int getSocketType() {
        return SocketType.COMPONENT_SOCKET;
    }

    @Override
    protected int getTimeoutSeconds() {
        return 30;
    }

    @Override
    protected void onConnection(Client webSocket) {
        super.onConnection(webSocket);
        webSocket.registerOnClose(this::onClientCloseHandler);
    }

    public void onClientCloseHandler(Object sender, EventObject<ConnectContext> obj) {
        ComponentService componentService = SpringContextUtils.getBean(ComponentService.class);
        LockRunner lockRunner = SpringContextUtils.getBean(LockRunner.class);
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
}
