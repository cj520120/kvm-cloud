package cn.ruiyun.cloud.management.websocket.cluster.process.component;

import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.data.entity.ComponentEntity;
import cn.ruiyun.cloud.management.data.entity.DnsEntity;
import cn.ruiyun.cloud.management.model.DnsModel;
import cn.ruiyun.cloud.management.servcie.ConvertService;
import cn.ruiyun.cloud.management.servcie.DnsService;
import cn.ruiyun.cloud.management.servcie.NetworkService;
import cn.ruiyun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.ruiyun.cloud.management.websocket.manager.ComponentClientManager;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
public class UpdateComponentDnsProcess extends AbstractClusterMessageProcess<List<DnsModel>> {

    @Autowired
    private DnsService dnsService;
    @Autowired
    private ConvertService convertService;
    @Autowired
    private NetworkService networkService;

    @SuppressWarnings("unchecked")
    @Override
    protected void doProcess(NotifyData<List<DnsModel>> msg) {
        List<DnsEntity> dnsList = this.dnsService.listLocalNetworkDns(msg.getId());
        List<DnsModel> dnsModelList = dnsList.stream().map(convertService::initDnsModel).collect(Collectors.toList());

        List<ComponentEntity> components = this.networkService.listNetworkComponent(msg.getId());
        NotifyData<List<DnsModel>> sendMsg = NotifyData.<List<DnsModel>>builder().id(msg.getId()).type(Constant.NotifyType.NOTIFY_DNS_UPDATE).data(dnsModelList).version(System.currentTimeMillis()).build();

        for (ComponentEntity component : components) {
            if (component.getComponentType() == Constant.ComponentType.ROUTE) {
                ComponentClientManager.send(component.getComponentId(), sendMsg, Constant.SocketCommand.COMPONENT_NOTIFY);
            }
        }
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_COMPONENT_DNS;
    }
}
