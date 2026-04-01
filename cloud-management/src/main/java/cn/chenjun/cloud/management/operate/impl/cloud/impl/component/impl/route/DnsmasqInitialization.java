package cn.chenjun.cloud.management.operate.impl.cloud.impl.component.impl.route;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.common.util.JinjavaParser;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.operate.impl.cloud.bean.CloudConfig;
import cn.chenjun.cloud.management.operate.impl.cloud.impl.component.impl.BaseInitialization;
import cn.chenjun.cloud.management.util.IpCalculate;
import cn.chenjun.cloud.management.util.ResourceUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DnsmasqInitialization extends BaseInitialization {
    private static final int MIN_DHCP_SIZE = 2;


    @Override
    public boolean isSupport(int componentType) {
        return Objects.equals(componentType, Constant.ComponentType.ROUTE);
    }

    @Override
    public void initialize(CloudConfig config, GuestEntity guest, NetworkEntity network, ComponentEntity component, ComponentGuestEntity componentGuest) {
        config.appendPackage("dnsmasq");
        config.appendFile("/etc/dnsmasq.conf", this.buildDnsmasqConfig(guest, network, component));
        config.appendRuncmd("mkdir -p /etc/dnsmasq.hosts.d");
        config.appendRuncmd("iptables -A INPUT -p udp --dport 53 -j ACCEPT");
        config.appendRuncmd("iptables -A INPUT -p tcp --dport 53 -j ACCEPT");
        config.appendRuncmd("iptables -A INPUT -p udp --dport 67 -j ACCEPT");
        config.appendRuncmd("iptables -A INPUT -p udp --dport 68 -j ACCEPT");

        config.appendRuncmd("systemctl enable dnsmasq");
        config.appendRuncmd("systemctl restart dnsmasq");
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private String buildDnsmasqConfig(GuestEntity guest, NetworkEntity network, ComponentEntity component) {
        List<GuestNetworkEntity> allGuestNetwork = new ArrayList<>(this.guestNetworkDao.listByNetworkId(network.getNetworkId()));
        Optional<GuestNetworkEntity> optional = allGuestNetwork.stream().filter(t -> Objects.equals(t.getAllocateId(), guest.getGuestId()) && Objects.equals(t.getAllocateType(), cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.GUEST)).findFirst();
        if (!optional.isPresent()) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "Dnsmasq初始化失败.未找到监听网卡,ComponentId=" + component.getComponentId() + ",GuestId=" + guest.getGuestId());
        }
        GuestNetworkEntity defaultGuestNetwork = optional.get();
        //删除VIP地址、网关地址、以及本机的IP地址
        allGuestNetwork.removeIf(t -> Objects.equals(t.getIp(), network.getGateway()) || Objects.equals(t.getIp(), component.getComponentVip()) || (Objects.equals(t.getAllocateId(), guest.getGuestId()) && Objects.equals(t.getAllocateType(), Constant.NetworkAllocateType.GUEST)));
        allGuestNetwork.sort(Comparator.comparingLong(o -> IpCalculate.ipToLong(o.getIp())));
        if (allGuestNetwork.size() < MIN_DHCP_SIZE) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "Dnsmasq初始化失败.没有可分配的区间,ComponentId=" + component.getComponentId());
        }
        String startIp = allGuestNetwork.get(0).getIp();
        String endIp = allGuestNetwork.get(allGuestNetwork.size() - 1).getIp();

        String config = ResourceUtil.readUtf8Str("tpl/component/init/dnsmasq.conf.json");
        Map<String, Object> map = new HashMap<>();
        map.put("interface", "eth" + defaultGuestNetwork.getDeviceId());
        map.put("ip", defaultGuestNetwork.getIp());
        map.put("vip", component.getComponentVip());
        map.put("startIp", startIp);
        map.put("endIp", endIp);
        map.put("gateway", network.getGateway());
        map.put("mask", network.getMask());
        map.put("domain", network.getDomain());
        map.put("dnsList", Arrays.asList(network.getDns().split(",")));
        List<Map<String, Object>> dhcpList = allGuestNetwork.stream().map(guestNetwork -> {
            Map<String, Object> dhcp = new HashMap<>(2);
            dhcp.put("mac", guestNetwork.getMac());
            dhcp.put("ip", guestNetwork.getIp());
            return dhcp;
        }).collect(Collectors.toList());
        map.put("dhcpList", dhcpList);
        String dnsmasqConfig = JinjavaParser.create().render(config, map);
        return dnsmasqConfig;
    }
}
