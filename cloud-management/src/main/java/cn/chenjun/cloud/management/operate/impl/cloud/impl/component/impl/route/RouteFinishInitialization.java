package cn.chenjun.cloud.management.operate.impl.cloud.impl.component.impl.route;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.ComponentGuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.impl.cloud.bean.CloudConfig;
import cn.chenjun.cloud.management.operate.impl.cloud.impl.component.impl.BaseInitialization;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RouteFinishInitialization extends BaseInitialization {
    @Override
    public boolean isSupport(int componentType) {
        return Objects.equals(componentType, Constant.ComponentType.ROUTE);
    }

    @Override
    public void initialize(CloudConfig config, GuestEntity guest, NetworkEntity network, ComponentEntity component, ComponentGuestEntity componentGuest) {
        config.appendRuncmd("iptables -A INPUT -p tcp --dport 80 -j ACCEPT");
        config.appendRuncmd("iptables -t nat -A PREROUTING -d 169.254.169.254 -p tcp --dport 80 -j DNAT --to-destination 127.0.0.1:80");
        config.appendRuncmd("iptables -t nat -A OUTPUT -d 169.254.169.254 -p tcp --dport 80 -j DNAT --to-destination 127.0.0.1:80");
        config.appendRuncmd("iptables -t nat -A PREROUTING -d 169.254.169.254 -p icmp --icmp-type echo-request -j DNAT --to-destination 127.0.0.1");
        config.appendRuncmd("iptables -t nat -A OUTPUT -d 169.254.169.254 -p icmp --icmp-type echo-request -j DNAT --to-destination 127.0.0.1");

    }

    @Override
    public int getOrder() {
        return 100;
    }
}
