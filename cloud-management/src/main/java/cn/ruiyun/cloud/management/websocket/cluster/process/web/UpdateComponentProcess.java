package cn.ruiyun.cloud.management.websocket.cluster.process.web;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.model.ComponentDetailModel;
import cn.ruiyun.cloud.management.servcie.ComponentService;
import cn.ruiyun.cloud.management.servcie.ConvertService;
import cn.ruiyun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.ruiyun.cloud.management.websocket.manager.WebClientManager;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateComponentProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private ComponentService componentService;
    @Autowired
    private ConvertService convertService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<ComponentDetailModel> resultUtil = this.getResourceData(() -> this.componentService.getComponentById(msg.getId()), this.convertService::initComponentModel);

        NotifyData<ResultUtil<ComponentDetailModel>> sendMsg = NotifyData.<ResultUtil<ComponentDetailModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_COMPONENT).data(resultUtil).version(System.currentTimeMillis()).build();
        WebClientManager.sendAll(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_COMPONENT;
    }
}
