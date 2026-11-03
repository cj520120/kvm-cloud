package cn.ruiyun.cloud.management.websocket.cluster.process.web;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.model.GroupModel;
import cn.ruiyun.cloud.management.servcie.ConvertService;
import cn.ruiyun.cloud.management.servcie.GroupService;
import cn.ruiyun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.ruiyun.cloud.management.websocket.manager.WebClientManager;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateGroupProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private GroupService groupService;
    @Autowired
    private ConvertService convertService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<GroupModel> resultUtil = this.getResourceData(() -> this.groupService.getGroupById(msg.getId()), this.convertService::initGroupModel);
        NotifyData<ResultUtil<GroupModel>> sendMsg = NotifyData.<ResultUtil<GroupModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_GROUP).data(resultUtil).version(System.currentTimeMillis()).build();
        WebClientManager.sendAll(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_GROUP;
    }
}
