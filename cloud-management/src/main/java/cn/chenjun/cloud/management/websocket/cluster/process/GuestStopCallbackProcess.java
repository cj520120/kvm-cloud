package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.servcie.UserService;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GuestStopCallbackProcess extends AbstractClusterMessageProcess {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private UserService userService;

    @Override
    public void process(NotifyData<?> msg) {
        NotifyData<?> sendMsg = NotifyData.builder().id(msg.getId()).type(Constant.NotifyType.GUEST_STOP_CALLBACK_NOTIFY).data(msg.getData()).version(System.currentTimeMillis()).build();
        wsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.GUEST_STOP_CALLBACK_NOTIFY;
    }
}
