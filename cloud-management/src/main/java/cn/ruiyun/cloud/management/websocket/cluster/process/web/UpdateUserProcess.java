package cn.ruiyun.cloud.management.websocket.cluster.process.web;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.model.UserModel;
import cn.ruiyun.cloud.management.servcie.ConvertService;
import cn.ruiyun.cloud.management.servcie.UserService;
import cn.ruiyun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.ruiyun.cloud.management.websocket.manager.WebClientManager;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateUserProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private UserService userService;
    @Autowired
    private ConvertService convertService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<UserModel> resultUtil = getResourceData(() -> userService.getUserInfo(msg.getId()), this.convertService::initUserModel);
        NotifyData<ResultUtil<UserModel>> sendMsg = NotifyData.<ResultUtil<UserModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_USER).data(resultUtil).version(System.currentTimeMillis()).build();
        WebClientManager.sendAll(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_USER;
    }
}
