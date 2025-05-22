package cn.chenjun.cloud.management.websocket.cluster.process.web;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.UserInfoModel;
import cn.chenjun.cloud.management.servcie.UserService;
import cn.chenjun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.manager.WebClientManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateUserProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private UserService userService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<UserInfoModel> resultUtil = this.userService.getUserInfo(msg.getId());
        NotifyData<ResultUtil<UserInfoModel>> sendMsg = NotifyData.<ResultUtil<UserInfoModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_USER).data(resultUtil).version(System.currentTimeMillis()).build();
        WebClientManager.sendAll(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_USER;
    }
}
