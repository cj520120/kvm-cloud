package cn.ruiyun.cloud.management.websocket.cluster.process.web;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.model.TemplateModel;
import cn.ruiyun.cloud.management.servcie.ConvertService;
import cn.ruiyun.cloud.management.servcie.TemplateService;
import cn.ruiyun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.ruiyun.cloud.management.websocket.manager.WebClientManager;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateTemplateProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private TemplateService templateService;
    @Autowired
    private ConvertService convertService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<TemplateModel> resultUtil = getResourceData(() -> this.templateService.getTemplateById(msg.getId()), this.convertService::initTemplateModel);
        NotifyData<ResultUtil<TemplateModel>> sendMsg = NotifyData.<ResultUtil<TemplateModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_TEMPLATE).data(resultUtil).version(System.currentTimeMillis()).build();
        WebClientManager.sendAll(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_TEMPLATE;
    }
}
