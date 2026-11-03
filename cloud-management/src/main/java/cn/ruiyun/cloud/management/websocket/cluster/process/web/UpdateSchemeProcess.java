package cn.ruiyun.cloud.management.websocket.cluster.process.web;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.model.SchemeModel;
import cn.ruiyun.cloud.management.servcie.ConvertService;
import cn.ruiyun.cloud.management.servcie.SchemeService;
import cn.ruiyun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.ruiyun.cloud.management.websocket.manager.WebClientManager;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateSchemeProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private SchemeService schemeService;
    @Autowired
    private ConvertService convertService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<SchemeModel> resultUtil = getResourceData(() -> this.schemeService.getSchemeInfo(msg.getId()), this.convertService::initSchemeModel);
        NotifyData<ResultUtil<SchemeModel>> sendMsg = NotifyData.<ResultUtil<SchemeModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_SCHEME).data(resultUtil).version(System.currentTimeMillis()).build();
        WebClientManager.sendAll(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_SCHEME;
    }
}
