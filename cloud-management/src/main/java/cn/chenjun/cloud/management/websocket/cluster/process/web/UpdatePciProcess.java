package cn.chenjun.cloud.management.websocket.cluster.process.web;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class UpdatePciProcess extends AbstractClusterMessageProcess<Void> {


    @Override
    protected void doProcess(NotifyData<Void> msg) {

    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_GUEST_PIC;
    }
}
