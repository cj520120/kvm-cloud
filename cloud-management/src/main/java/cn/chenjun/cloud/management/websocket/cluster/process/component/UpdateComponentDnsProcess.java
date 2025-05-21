package cn.chenjun.cloud.management.websocket.cluster.process.component;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.model.DnsModel;
import cn.chenjun.cloud.management.servcie.DnsService;
import cn.chenjun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.management.websocket.util.WsSessionManager;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chenjun
 */
@Component
public class UpdateComponentDnsProcess extends AbstractClusterMessageProcess<List<DnsModel>> {

    @Autowired
    private DnsService dnsService;
    @Autowired
    private ComponentMapper mapper;

    @SuppressWarnings("unchecked")
    @Override
    protected void doProcess(NotifyData<List<DnsModel>> msg) {
        List<DnsModel> dnsModelList = msg.getData();
        if (dnsModelList == null) {
            dnsModelList = this.dnsService.listLocalNetworkDns(msg.getId());
        }
        List<ComponentEntity> components = mapper.selectList(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.NETWORK_ID, msg.getId()));
        NotifyData<List<DnsModel>> sendMsg = NotifyData.<List<DnsModel>>builder().id(msg.getId()).type(Constant.NotifyType.COMPONENT_UPDATE_DNS).data(dnsModelList).version(System.currentTimeMillis()).build();

        for (ComponentEntity component : components) {
            WsSessionManager.sendComponentNotify(component.getComponentId(), sendMsg);
        }
    }

    @Override
    public int getType() {
        return Constant.NotifyType.COMPONENT_UPDATE_DNS;
    }
}
