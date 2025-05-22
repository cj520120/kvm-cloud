package cn.chenjun.cloud.management.websocket.cluster.process.web;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.SshAuthorizedModel;
import cn.chenjun.cloud.management.servcie.SshAuthorizedService;
import cn.chenjun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.manager.WebClientManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateSshKeyProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private SshAuthorizedService sshAuthorizedService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<SshAuthorizedModel> resultUtil = this.sshAuthorizedService.getSshKey(msg.getId());
        NotifyData<ResultUtil<SshAuthorizedModel>> sendMsg = NotifyData.<ResultUtil<SshAuthorizedModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_SSH).data(resultUtil).version(System.currentTimeMillis()).build();
        WebClientManager.sendAll(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_SSH;
    }
}
