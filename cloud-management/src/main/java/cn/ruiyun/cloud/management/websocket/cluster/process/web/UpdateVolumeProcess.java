package cn.ruiyun.cloud.management.websocket.cluster.process.web;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.data.entity.GuestEntity;
import cn.ruiyun.cloud.management.model.VolumeModel;
import cn.ruiyun.cloud.management.servcie.ConvertService;
import cn.ruiyun.cloud.management.servcie.VolumeService;
import cn.ruiyun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.ruiyun.cloud.management.websocket.manager.WebClientManager;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateVolumeProcess extends AbstractClusterMessageProcess<ResultUtil<GuestEntity>> {

    @Autowired
    private VolumeService volumeService;
    @Autowired
    private ConvertService convertService;

    @Override
    protected void doProcess(NotifyData<ResultUtil<GuestEntity>> msg) {
        ResultUtil<VolumeModel> resultUtil = this.getResourceData(() -> volumeService.getVolumeById(msg.getId()), this.convertService::initVolumeModel);
        NotifyData<ResultUtil<VolumeModel>> sendMsg = NotifyData.<ResultUtil<VolumeModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_VOLUME).data(resultUtil).version(System.currentTimeMillis()).build();
        WebClientManager.sendAll(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_VOLUME;
    }
}
