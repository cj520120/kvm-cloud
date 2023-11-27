package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.management.model.VncModel;
import cn.chenjun.cloud.management.servcie.VncService;
import cn.chenjun.cloud.management.websocket.WsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UpdateVncProcess implements ClusterProcess {
    @Autowired
    private WsManager wsManager;
    @Autowired
    private VncService vncService;

    @Override
    public void process(NotifyData<?> msg) {
        List<VncModel> vncModelList = (List<VncModel>) msg.getData();
        if (vncModelList == null) {
            vncModelList = this.vncService.listVncByNetworkId(msg.getId());
        }
        NotifyData<List<VncModel>> sendMsg = NotifyData.<List<VncModel>>builder().type(Constant.NotifyType.COMPONENT_UPDATE_VNC).data(vncModelList).build();
        wsManager.sendComponentNotify(msg.getId(), sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.COMPONENT_UPDATE_VNC;
    }
}
