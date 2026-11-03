package cn.ruiyun.cloud.management.websocket.cluster.process.web;

import cn.ruiyun.cloud.common.util.Constant;
import org.springframework.stereotype.Component;

@Component
public class GuestStopActionCallbackProcess extends AbstractGuestActionCallbackProcess {


    @Override
    public int getType() {
        return Constant.NotifyType.GUEST_STOP_CALLBACK_NOTIFY;
    }


    @Override
    protected int getNotifyType() {
        return Constant.NotifyType.GUEST_STOP_CALLBACK_NOTIFY;
    }
}
