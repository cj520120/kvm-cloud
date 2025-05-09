package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.DnsModel;
import cn.chenjun.cloud.management.model.UserInfoModel;
import cn.chenjun.cloud.management.servcie.UserService;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateUserProcess extends AbstractClusterMessageProcess {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private UserService userService;

    @Override
    public void process(NotifyData<?> msg) {
        ResultUtil<UserInfoModel> resultUtil = this.userService.getUserInfo(msg.getId());
        NotifyData<ResultUtil<UserInfoModel>> sendMsg = NotifyData.<ResultUtil<UserInfoModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_USER).data(resultUtil).version(System.currentTimeMillis()).build();
        wsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_USER;
    }
}
