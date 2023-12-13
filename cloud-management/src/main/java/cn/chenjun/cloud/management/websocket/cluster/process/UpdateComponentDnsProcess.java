package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.DnsModel;
import cn.chenjun.cloud.management.servcie.DnsService;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chenjun
 */
@Component
public class UpdateComponentDnsProcess extends AbstractClusterMessageProcess {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private DnsService dnsService;

    @Override
    public void process(NotifyData<?> msg) {
        List<DnsModel> dnsModelList = (List<DnsModel>) msg.getData();
        if (dnsModelList == null) {
            dnsModelList = this.dnsService.listLocalNetworkDns(msg.getId());
        }
        NotifyData<List<DnsModel>> sendMsg = NotifyData.<List<DnsModel>>builder().id(msg.getId()).type(Constant.NotifyType.COMPONENT_UPDATE_DNS).data(dnsModelList).build();
        wsSessionManager.sendNetworkNotify(msg.getId(), sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.COMPONENT_UPDATE_DNS;
    }
}
