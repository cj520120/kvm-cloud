package cn.chenjun.cloud.management.websocket.cluster.process.web;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.TemplateModel;
import cn.chenjun.cloud.management.servcie.TemplateService;
import cn.chenjun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.management.websocket.util.WsSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateTemplateProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private TemplateService templateService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<TemplateModel> resultUtil = this.templateService.getTemplateInfo(msg.getId());
        NotifyData<ResultUtil<TemplateModel>> sendMsg = NotifyData.<ResultUtil<TemplateModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_TEMPLATE).data(resultUtil).version(System.currentTimeMillis()).build();
        WsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_TEMPLATE;
    }
}
