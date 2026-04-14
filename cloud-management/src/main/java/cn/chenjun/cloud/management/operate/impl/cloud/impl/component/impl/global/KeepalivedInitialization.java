package cn.chenjun.cloud.management.operate.impl.cloud.impl.component.impl.global;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.JinjavaParser;
import cn.chenjun.cloud.common.util.ResourceUtil;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.operate.impl.cloud.bean.CloudConfig;
import cn.chenjun.cloud.management.operate.impl.cloud.impl.component.impl.BaseInitialization;
import cn.chenjun.cloud.management.util.KeepalivedConfigGenerator;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class KeepalivedInitialization extends BaseInitialization {
    public static final int MASTER_PRIORITY = 100;
    public static final int SLAVE_PRIORITY = 90;

    @Override
    public boolean isSupport(int componentType) {
        return true;
    }

    @Override
    public void initialize(CloudConfig config, GuestEntity guest, NetworkEntity network, ComponentEntity component, ComponentGuestEntity componentGuest) {
        config.appendPackage("keepalived");
        config.appendFile("/etc/keepalived/keepalived.conf", this.buildKeepaliveConfig(component, guest, componentGuest));
        config.appendRuncmd("systemctl enable keepalived");
        config.appendRuncmd("systemctl restart keepalived");
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private Map<String, Object> buildKeepAliveParam(String networkPoolId, String nic, String vip, ComponentEntity component) {

        Map<String, Object> map = new HashMap<>(3);
        map.put("networkInterface", nic);
        map.put("virtualIpAddress", vip);
        map.put("virtualRouterId", KeepalivedConfigGenerator.generateVrid(networkPoolId, component.getComponentId()));
        map.put("password", KeepalivedConfigGenerator.generatePassword(networkPoolId, component.getComponentId()));
        return map;
    }

    private String buildKeepaliveConfig(ComponentEntity component, GuestEntity guest, ComponentGuestEntity componentGuest) {
        List<GuestNetworkEntity> guestNicList = this.guestNetworkDao.listByAllocate(Constant.NetworkAllocateType.GUEST, guest.getGuestId());
        List<Map<String, Object>> vrrpList = new ArrayList<>(1);
        NetworkEntity network = this.networkDao.findById(component.getNetworkId());
        for (int i = 0; i < guestNicList.size(); i++) {
            GuestNetworkEntity guestNetwork = guestNicList.get(i);
            String nic = "eth" + i;
            String vip = null;
            if (Objects.equals(network.getNetworkId(), guestNetwork.getNetworkId())) {
                vip = component.getComponentVip();
            } else if (Objects.equals(network.getBasicNetworkId(), guestNetwork.getNetworkId())) {
                vip = component.getBasicComponentVip();
            }
            if (ObjectUtils.isNotEmpty(vip)) {
                vrrpList.add(buildKeepAliveParam(network.getPoolId(), nic, vip, component));
            }
        }
        Map<String, Object> map = new HashMap<>(1);
        map.put("list", vrrpList);
        map.put("routeId", KeepalivedConfigGenerator.generateRouterId(network.getPoolId(), component.getComponentId()));
        map.put("vrrpMcastGroup4", KeepalivedConfigGenerator.generateMcastGroup4(network.getPoolId(), component.getComponentId()));
        String config = ResourceUtil.readUtf8Str("tpl/component/init/keepalived.conf.json");
        return JinjavaParser.create().render(config, map);
    }
}
