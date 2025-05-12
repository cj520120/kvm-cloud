package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.SchemeModel;
import cn.chenjun.cloud.management.servcie.SchemeService;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateSchemeProcess extends AbstractClusterMessageProcess<Void> {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private SchemeService schemeService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<SchemeModel> resultUtil = this.schemeService.getSchemeInfo(msg.getId());
        NotifyData<ResultUtil<SchemeModel>> sendMsg = NotifyData.<ResultUtil<SchemeModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_SCHEME).data(resultUtil).version(System.currentTimeMillis()).build();
        wsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_SCHEME;
    }
}
