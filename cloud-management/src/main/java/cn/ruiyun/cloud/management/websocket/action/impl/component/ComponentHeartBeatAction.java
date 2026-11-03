package cn.ruiyun.cloud.management.websocket.action.impl.component;

import cn.ruiyun.cloud.common.socket.packet.WsMessage;
import cn.ruiyun.cloud.common.socket.packet.data.base.MapData;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.data.entity.ComponentGuestEntity;
import cn.ruiyun.cloud.management.servcie.ComponentService;
import cn.ruiyun.cloud.management.servcie.LockRunner;
import cn.ruiyun.cloud.management.util.RedisKeyUtil;
import cn.ruiyun.cloud.management.websocket.action.WsAction;
import cn.ruiyun.cloud.management.websocket.listen.client.Client;
import cn.ruiyun.cloud.management.websocket.listen.context.ComponentContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

@Component
public class ComponentHeartBeatAction implements WsAction<MapData> {
    @Autowired
    private LockRunner lockRunner;
    @Autowired
    private ComponentService componentService;

    @Override
    public void doAction(Client webSocket, WsMessage<MapData> msg) throws IOException {
        if (webSocket.getContext() != null) {
            ComponentContext context = (ComponentContext) webSocket.getContext();
            if (context != null) {
                //设置网络组件心跳
                lockRunner.lockCall(RedisKeyUtil.getGlobalLockKey(), () -> {
                    ComponentGuestEntity componentGuest = componentService.findComponentGuestById(context.getComponentGuestId());
                    if (componentGuest != null && Objects.equals(componentGuest.getSessionId(), context.getSessionId())) {
                        componentGuest.setLastActiveTime(new Date(System.currentTimeMillis()));
                        componentGuest.setStatus(Constant.ComponentGuestStatus.ONLINE);
                        componentGuest.setErrorCount(0);
                        componentService.updateComponentGuest(componentGuest);
                    } else {
                        webSocket.close();
                    }
                    return null;
                });
            }
        }
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.COMPONENT_HEART_BEAT;
    }
}
