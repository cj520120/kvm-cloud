package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.util.Constant;
import org.springframework.stereotype.Component;

@Component
public class GuestStartActionCallbackProcess extends AbstractGuestActionCallbackProcess {


    @Override
    public int getType() {
        return Constant.NotifyType.GUEST_START_CALLBACK_NOTIFY;
    }

    @Override
    protected int getNotifyType() {
        return Constant.NotifyType.GUEST_START_CALLBACK_NOTIFY;
    }
}
