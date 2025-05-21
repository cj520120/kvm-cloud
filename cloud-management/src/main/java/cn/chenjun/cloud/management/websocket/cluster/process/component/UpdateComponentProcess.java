package cn.chenjun.cloud.management.websocket.cluster.process.component;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.model.ComponentDetailModel;
import cn.chenjun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.management.websocket.util.WsSessionManager;
import com.google.common.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chenjun
 */
@Component
public class UpdateComponentProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private ComponentMapper mapper;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ComponentEntity entity = this.mapper.selectById(msg.getId());
        ResultUtil<ComponentDetailModel> resultUtil;
        if (entity != null) {
            ComponentDetailModel model = ComponentDetailModel.builder().componentId(entity.getComponentId())
                    .networkId(entity.getNetworkId())
                    .componentSlaveNumber(entity.getComponentSlaveNumber())
                    .componentType(entity.getComponentType())
                    .masterGuestId(entity.getMasterGuestId())
                    .componentVip(entity.getComponentVip())
                    .basicComponentVip(entity.getBasicComponentVip())
                    .slaveGuestIds(GsonBuilderUtil.create().fromJson(entity.getSlaveGuestIds(), new TypeToken<List<Integer>>() {
                    }.getType()))
                    .createTime(entity.getCreateTime()).build();
            resultUtil = ResultUtil.success(model);
        } else {
            resultUtil = ResultUtil.error(ErrorCode.NETWORK_COMPONENT_NOT_FOUND, "网络组件未找到");
        }

        NotifyData<ResultUtil<ComponentDetailModel>> sendMsg = NotifyData.<ResultUtil<ComponentDetailModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_COMPONENT).data(resultUtil).version(System.currentTimeMillis()).build();
        WsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_COMPONENT;
    }
}
