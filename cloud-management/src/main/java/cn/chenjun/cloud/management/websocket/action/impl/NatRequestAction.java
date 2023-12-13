package cn.chenjun.cloud.management.websocket.action.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.NatEntity;
import cn.chenjun.cloud.management.data.mapper.NatMapper;
import cn.chenjun.cloud.management.model.NatModel;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.client.WsClient;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
public class NatRequestAction implements WsAction {
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private NetworkService networkService;

    @Override
    public void doAction(Session session, WsRequest msg) throws IOException {
        WsClient client = wsSessionManager.getClient(session);
        ResultUtil<List<NatModel>> resultUtil = this.networkService.listComponentNat(client.getComponentId());
        NotifyData<List<NatModel>> sendMsg = NotifyData.<List<NatModel>>builder().type(Constant.NotifyType.COMPONENT_UPDATE_NAT).data(resultUtil.getData()).build();
        WsMessage<NotifyData<List<NatModel>>> wsMessage = WsMessage.<NotifyData<List<NatModel>>>builder().command(Constant.SocketCommand.COMPONENT_NOTIFY).data(sendMsg).build();
        client.send(wsMessage);
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.COMPONENT_NAT_REQUEST;
    }
}
