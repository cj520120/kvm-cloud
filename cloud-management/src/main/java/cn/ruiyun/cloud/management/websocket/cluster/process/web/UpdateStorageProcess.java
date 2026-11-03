package cn.ruiyun.cloud.management.websocket.cluster.process.web;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.model.StorageModel;
import cn.ruiyun.cloud.management.servcie.ConvertService;
import cn.ruiyun.cloud.management.servcie.StorageService;
import cn.ruiyun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.ruiyun.cloud.management.websocket.manager.WebClientManager;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateStorageProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private StorageService storageService;
    @Autowired
    private ConvertService convertService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<StorageModel> resultUtil = getResourceData(() -> this.storageService.getStorageInfo(msg.getId()), this.convertService::initStorageModel);
        NotifyData<ResultUtil<StorageModel>> sendMsg = NotifyData.<ResultUtil<StorageModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_STORAGE).data(resultUtil).version(System.currentTimeMillis()).build();
        WebClientManager.sendAll(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_STORAGE;
    }
}
