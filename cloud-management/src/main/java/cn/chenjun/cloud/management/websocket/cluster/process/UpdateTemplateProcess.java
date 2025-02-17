package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.TemplateModel;
import cn.chenjun.cloud.management.servcie.TemplateService;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateTemplateProcess extends AbstractClusterMessageProcess {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private TemplateService templateService;

    @Override
    public void process(NotifyData<?> msg) {
        ResultUtil<TemplateModel> resultUtil = this.templateService.getTemplateInfo(msg.getId());
        NotifyData<ResultUtil<TemplateModel>> sendMsg = NotifyData.<ResultUtil<TemplateModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_TEMPLATE).data(resultUtil).version(System.currentTimeMillis()).build();
        wsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_TEMPLATE;
    }
}
