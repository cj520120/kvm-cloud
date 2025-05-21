package cn.chenjun.cloud.management.websocket.cluster.process.web;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.DnsModel;
import cn.chenjun.cloud.management.servcie.DnsService;
import cn.chenjun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.management.websocket.util.WsSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdateDnsProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private DnsService dnsService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {
        ResultUtil<DnsModel> resultUtil = this.dnsService.getDnsInfo(msg.getId());
        NotifyData<ResultUtil<DnsModel>> sendMsg = NotifyData.<ResultUtil<DnsModel>>builder().id(msg.getId()).type(Constant.NotifyType.UPDATE_DNS).data(resultUtil).version(System.currentTimeMillis()).build();
        WsSessionManager.sendWebNotify(sendMsg);
    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_DNS;
    }
}
