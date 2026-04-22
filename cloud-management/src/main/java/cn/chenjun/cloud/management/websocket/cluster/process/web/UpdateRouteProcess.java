package cn.chenjun.cloud.management.websocket.cluster.process.web;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.RouteStrategyModel;
import cn.chenjun.cloud.management.servcie.ComponentService;
import cn.chenjun.cloud.management.servcie.ConvertService;
import cn.chenjun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.manager.WebClientManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateRouteProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private ComponentService componentService;
    @Autowired
    private ConvertService convertService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<RouteStrategyModel> resultUtil = this.getResourceData(() -> this.componentService.findRouteStrategyById(msg.getId()), this.convertService::initRouteStrategy);
        NotifyData<ResultUtil<RouteStrategyModel>> sendMsg = NotifyData.<ResultUtil<RouteStrategyModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_ROUTE).data(resultUtil).version(System.currentTimeMillis()).build();
        WebClientManager.sendAll(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_ROUTE;
    }
}
