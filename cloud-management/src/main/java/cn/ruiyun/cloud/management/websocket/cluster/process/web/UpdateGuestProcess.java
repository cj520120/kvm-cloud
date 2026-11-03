package cn.ruiyun.cloud.management.websocket.cluster.process.web;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.model.GuestModel;
import cn.ruiyun.cloud.management.servcie.ConvertService;
import cn.ruiyun.cloud.management.servcie.GuestService;
import cn.ruiyun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.ruiyun.cloud.management.websocket.manager.WebClientManager;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateGuestProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private GuestService guestService;
    @Autowired
    private ConvertService convertService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<GuestModel> resultUtil = this.getResourceData(() -> this.guestService.getGuestInfo(msg.getId()), this.convertService::initGuestModel);
        NotifyData<ResultUtil<GuestModel>> sendMsg = NotifyData.<ResultUtil<GuestModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_GUEST).data(resultUtil).version(System.currentTimeMillis()).build();
        WebClientManager.sendAll(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_GUEST;
    }

}
