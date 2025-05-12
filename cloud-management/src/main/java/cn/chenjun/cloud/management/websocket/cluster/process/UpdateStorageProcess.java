package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.StorageModel;
import cn.chenjun.cloud.management.servcie.StorageService;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateStorageProcess extends AbstractClusterMessageProcess<Void> {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private StorageService storageService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<StorageModel> resultUtil = this.storageService.getStorageInfo(msg.getId());
        NotifyData<ResultUtil<StorageModel>> sendMsg = NotifyData.<ResultUtil<StorageModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_STORAGE).data(resultUtil).version(System.currentTimeMillis()).build();
        wsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_STORAGE;
    }
}
