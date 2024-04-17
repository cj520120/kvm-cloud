package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.GroupModel;
import cn.chenjun.cloud.management.model.SshAuthorizedModel;
import cn.chenjun.cloud.management.servcie.GroupService;
import cn.chenjun.cloud.management.servcie.SshAuthorizedService;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateSshKeyProcess extends AbstractClusterMessageProcess {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private SshAuthorizedService sshAuthorizedService;

    @Override
    public void process(NotifyData<?> msg) {
        ResultUtil<SshAuthorizedModel> resultUtil = this.sshAuthorizedService.getSshKey(msg.getId());
        NotifyData<ResultUtil<SshAuthorizedModel>> sendMsg = NotifyData.<ResultUtil<SshAuthorizedModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_SSH_KEY).data(resultUtil).build();
        wsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_SSH_KEY;
    }
}
