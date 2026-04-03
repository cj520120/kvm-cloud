package cn.chenjun.cloud.management.websocket.action.impl.component;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.base.MapData;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.SecurityUtil;
import cn.chenjun.cloud.management.data.entity.ComponentGuestEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.servcie.ComponentService;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.listen.context.ComponentContext;
import cn.chenjun.cloud.management.websocket.manager.ComponentClientManager;
import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class ComponentConnectionAction implements WsAction<MapData> {

    private static final int COMPONENT_STATUS_SUCCESS = 0;
    @Autowired
    private NetworkService networkService;
    @Autowired
    private ComponentService componentService;
    @Autowired
    private LockRunner lockRunner;

    @Override
    public void doAction(Client webSocket, WsMessage<MapData> msg) throws IOException {
        Map<String, Object> params = msg.getData();
        int networkId = NumberUtil.parseInt(params.getOrDefault("networkId", "0").toString());
        NetworkEntity network = this.networkService.getNetworkInfo(networkId);
        String sign = params.remove("sign").toString();
        if (SecurityUtil.signature(params, network.getSecret()).equals(sign)) {
            String sessionId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            ComponentContext context = GsonBuilderUtil.create().fromJson(params.toString(), ComponentContext.class);
            context.setSessionId(sessionId);
            webSocket.login(context);
            ComponentClientManager.addComponentClient(context.getComponentId(), webSocket);
            ComponentGuestEntity loginComponentGuest = lockRunner.lockCall(RedisKeyUtil.getGlobalLockKey(), () -> {
                ComponentGuestEntity componentGuest = componentService.findComponentGuestById(context.getComponentGuestId());
                if (componentGuest != null) {
                    if (context.getStatus() == COMPONENT_STATUS_SUCCESS) {
                        componentGuest.setErrorCount(0);
                        componentGuest.setStatus(Constant.ComponentGuestStatus.ONLINE);
                    } else {
                        componentGuest.setStatus(Constant.ComponentGuestStatus.OFFLINE);
                        componentGuest.setErrorCount(1000);
                    }
                    componentGuest.setSessionId(sessionId);
                    componentGuest.setLastActiveTime(new Date(System.currentTimeMillis()));
                    componentService.updateComponentGuest(componentGuest);
                    log.info("component guest online {}", componentGuest);
                }
                return componentGuest;
            });
            WsMessage<Void> wsMessage;
            if (loginComponentGuest == null) {
                wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.COMPONENT_CONNECT_FAIL).build();
            } else {
                wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.COMPONENT_CONNECT_SUCCESS).build();
            }
            webSocket.sendJsonPacket(wsMessage);
        } else {
            WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.COMPONENT_CONNECT_FAIL).build();
            webSocket.sendJsonPacket(wsMessage);
        }
    }


    @Override
    public int getCommand() {
        return Constant.SocketCommand.COMPONENT_CONNECT;
    }
}
