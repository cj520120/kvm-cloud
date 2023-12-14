package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.model.VncModel;
import cn.chenjun.cloud.management.servcie.VncService;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chenjun
 */
@Component
public class UpdateComponentVncProcess extends AbstractClusterMessageProcess {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private VncService vncService;

    @Autowired
    private ComponentMapper mapper;

    @SuppressWarnings("unchecked")
    @Override
    public void process(NotifyData<?> msg) {
        List<VncModel> vncModelList = (List<VncModel>) msg.getData();
        if (vncModelList == null) {
            vncModelList = this.vncService.listVncByNetworkId(msg.getId());
        }
        NotifyData<List<VncModel>> sendMsg = NotifyData.<List<VncModel>>builder().id(msg.getId()).type(Constant.NotifyType.COMPONENT_UPDATE_VNC).data(vncModelList).build();
        List<ComponentEntity> components = mapper.selectList(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.NETWORK_ID, msg.getId()));
        for (ComponentEntity component : components) {
            wsSessionManager.sendComponentNotify(component.getComponentId(), sendMsg);
        }
    }

    @Override
    public int getType() {
        return Constant.NotifyType.COMPONENT_UPDATE_VNC;
    }
}
