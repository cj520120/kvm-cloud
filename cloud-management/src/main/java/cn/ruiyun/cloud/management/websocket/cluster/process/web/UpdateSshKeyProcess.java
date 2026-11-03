package cn.ruiyun.cloud.management.websocket.cluster.process.web;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.model.SshAuthorizedModel;
import cn.ruiyun.cloud.management.servcie.ConvertService;
import cn.ruiyun.cloud.management.servcie.SshAuthorizedService;
import cn.ruiyun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.ruiyun.cloud.management.websocket.manager.WebClientManager;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateSshKeyProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private SshAuthorizedService sshAuthorizedService;
    @Autowired
    private ConvertService convertService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<SshAuthorizedModel> resultUtil = getResourceData(() -> this.sshAuthorizedService.getSshKey(msg.getId()), this.convertService::initSshModel);
        NotifyData<ResultUtil<SshAuthorizedModel>> sendMsg = NotifyData.<ResultUtil<SshAuthorizedModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_SSH).data(resultUtil).version(System.currentTimeMillis()).build();
        WebClientManager.sendAll(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_SSH;
    }
}
