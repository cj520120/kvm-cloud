package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.model.ComponentModel;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.common.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chenjun
 */
@Component
public class UpdateComponentProcess extends AbstractClusterMessageProcess {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private ComponentMapper mapper;

    @Override
    public void process(NotifyData<?> msg) {
        ComponentEntity entity = this.mapper.selectById(msg.getId());
        ResultUtil<ComponentModel> resultUtil;
        if (entity != null) {
            ComponentModel model = ComponentModel.builder().componentId(entity.getComponentId())
                    .networkId(entity.getNetworkId())
                    .componentSlaveNumber(entity.getComponentSlaveNumber())
                    .componentType(entity.getComponentType())
                    .masterGuestId(entity.getMasterGuestId())
                    .componentVip(entity.getComponentVip())
                    .basicComponentVip(entity.getBasicComponentVip())
                    .slaveGuestIds(GsonBuilderUtil.create().fromJson(entity.getSlaveGuestIds(), new TypeToken<List<Integer>>() {
                    }.getType())).build();
            resultUtil = ResultUtil.success(model);
        } else {
            resultUtil = ResultUtil.error(ErrorCode.NETWORK_COMPONENT_NOT_FOUND, "网络组件未找到");
        }

        NotifyData<ResultUtil<ComponentModel>> sendMsg = NotifyData.<ResultUtil<ComponentModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_COMPONENT).data(resultUtil).build();
        wsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_COMPONENT;
    }
}
