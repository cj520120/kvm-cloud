package cn.chenjun.cloud.management.websocket.cluster.process.web;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.StorageModel;
import cn.chenjun.cloud.management.servcie.StorageService;
import cn.chenjun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.management.websocket.util.WsSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateStorageProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private StorageService storageService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<StorageModel> resultUtil = this.storageService.getStorageInfo(msg.getId());
        NotifyData<ResultUtil<StorageModel>> sendMsg = NotifyData.<ResultUtil<StorageModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_STORAGE).data(resultUtil).version(System.currentTimeMillis()).build();
        WsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_STORAGE;
    }
}
