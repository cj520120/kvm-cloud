package cn.chenjun.cloud.management.websocket.action.impl.component;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.base.MapData;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.RouteStrategyEntity;
import cn.chenjun.cloud.management.servcie.ComponentService;
import cn.chenjun.cloud.management.servcie.ConvertService;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.listen.context.ComponentContext;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
public class RouteRequestAction implements WsAction<MapData> {
    @Autowired
    private ComponentService componentService;
    @Autowired
    private ConvertService convertService;

    @Override
    public void doAction(Client webSocket, WsMessage<MapData> msg) throws IOException {
        ComponentContext context = (ComponentContext) webSocket.getContext();
        if (context == null) {
            webSocket.close();
            return;
        }
        List<RouteStrategyEntity> routes = this.componentService.listRouteStrategyByComponentId(context.getComponentId());
        List<RouteDispatch.Rule> rules = routes.stream().map(route -> RouteDispatch.Rule.builder().ip(route.getDestIp()).cidr(route.getCidr()).nexthop(route.getNexthop()).build()).collect(Collectors.toList());
        RouteDispatch routeDispatch = RouteDispatch.builder().rules(rules).interfaceName("eth0").build();
        NotifyData<RouteDispatch> sendMsg = NotifyData.<RouteDispatch>builder().type(Constant.NotifyType.NOTIFY_ROUTE_UPDATE).data(routeDispatch).build();
        WsMessage<NotifyData<RouteDispatch>> wsMessage = WsMessage.<NotifyData<RouteDispatch>>builder().command(Constant.SocketCommand.COMPONENT_NOTIFY).data(sendMsg).build();
        webSocket.sendJsonPacket(wsMessage);
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.COMPONENT_ROUTE_REQUEST;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteDispatch {
        private List<Rule> rules;
        @SerializedName("interface")
        private String interfaceName;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Rule {
            private String ip;
            private int cidr;
            private String nexthop;
        }
    }
}
