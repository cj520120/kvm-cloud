package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.GroupModel;
import cn.chenjun.cloud.management.servcie.GroupService;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateGroupProcess extends AbstractClusterMessageProcess {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private GroupService groupService;

    @Override
    public void process(NotifyData<?> msg) {
        ResultUtil<GroupModel> resultUtil = this.groupService.getGroup(msg.getId());
        NotifyData<ResultUtil<GroupModel>> sendMsg = NotifyData.<ResultUtil<GroupModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_GROUP).data(resultUtil).version(System.currentTimeMillis()).build();
        wsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_GROUP;
    }
}
