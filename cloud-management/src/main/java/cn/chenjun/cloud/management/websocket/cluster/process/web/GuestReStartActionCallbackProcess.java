package cn.chenjun.cloud.management.websocket.cluster.process.web;

import cn.chenjun.cloud.common.util.Constant;
import org.springframework.stereotype.Component;

@Component
public class GuestReStartActionCallbackProcess extends AbstractGuestActionCallbackProcess {


    @Override
    public int getType() {
        return Constant.NotifyType.GUEST_RESTART_CALLBACK_NOTIFY;
    }

    @Override
    protected int getNotifyType() {
        return Constant.NotifyType.GUEST_RESTART_CALLBACK_NOTIFY;
    }
}
