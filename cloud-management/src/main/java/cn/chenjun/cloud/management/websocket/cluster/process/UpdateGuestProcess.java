package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.GuestModel;
import cn.chenjun.cloud.management.servcie.GuestService;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateGuestProcess extends AbstractClusterMessageProcess {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private GuestService guestService;

    @Override
    public void process(NotifyData<?> msg) {
        ResultUtil<GuestModel> resultUtil = this.guestService.getGuestInfo(msg.getId());
        NotifyData<ResultUtil<GuestModel>> sendMsg = NotifyData.<ResultUtil<GuestModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_GUEST).data(resultUtil).version(System.currentTimeMillis()).build();
        wsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_GUEST;
    }
}
