package cn.chenjun.cloud.management.websocket.cluster.process.component;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.RouteStrategyEntity;
import cn.chenjun.cloud.management.servcie.ComponentService;
import cn.chenjun.cloud.management.servcie.ConvertService;
import cn.chenjun.cloud.management.websocket.action.impl.component.RouteRequestAction;
import cn.chenjun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.manager.ComponentClientManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
public class UpdateComponentRouteProcess extends AbstractClusterMessageProcess<Void> {

    @Autowired
    private ComponentService componentService;
    @Autowired
    private ConvertService convertService;

    @Override
    protected void doProcess(NotifyData<Void> msg) {

        List<RouteStrategyEntity> routes = this.componentService.listRouteStrategyByComponentId(msg.getId());
        List<RouteRequestAction.RouteDispatch.Rule> rules = routes.stream().map(route -> RouteRequestAction.RouteDispatch.Rule.builder().ip(route.getDestIp()).cidr(route.getCidr()).nexthop(route.getNexthop()).build()).collect(Collectors.toList());
        RouteRequestAction.RouteDispatch routeDispatch = RouteRequestAction.RouteDispatch.builder().rules(rules).interfaceName("eth0").build();
        NotifyData<RouteRequestAction.RouteDispatch> sendMsg = NotifyData.<RouteRequestAction.RouteDispatch>builder().type(Constant.NotifyType.NOTIFY_ROUTE_UPDATE).data(routeDispatch).build();
        ComponentClientManager.send(msg.getId(), sendMsg, Constant.SocketCommand.COMPONENT_NOTIFY);

    }

    @Override
    public int getType() {
        return Constant.NotifyType.UPDATE_COMPONENT_ROUTE;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteDispatch {
        private List<RouteRequestAction.RouteDispatch.Rule> rules;
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
