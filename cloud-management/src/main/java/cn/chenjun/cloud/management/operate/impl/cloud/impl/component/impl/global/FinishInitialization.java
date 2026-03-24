package cn.chenjun.cloud.management.operate.impl.cloud.impl.component.impl.global;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.operate.impl.cloud.bean.CloudConfig;
import cn.chenjun.cloud.management.operate.impl.cloud.impl.component.impl.BaseInitialization;
import cn.chenjun.cloud.management.util.ConfigKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class FinishInitialization extends BaseInitialization {

    @Override
    public boolean isSupport(int componentType) {
        return true;
    }


    @Override
    public void initialize(CloudConfig config, GuestEntity guest, NetworkEntity network, ComponentEntity component, ComponentGuestEntity componentGuest) {

        Map<String, Object> cloudConfig = new HashMap<>();
        cloudConfig.put("SERVER_URL", this.configService.getConfig(ConfigKey.DEFAULT_MANAGER_URI).toString().replace("http", "ws"));
        cloudConfig.put("COMPONENT_SECRET", network.getSecret());
        cloudConfig.put("NETWORK_ID", network.getNetworkId());
        cloudConfig.put("GUEST_ID", guest.getGuestId());
        cloudConfig.put("COMPONENT_GUEST_ID", componentGuest.getComponentGuestId());
        cloudConfig.put("COMPONENT_ID", component.getComponentId());
        cloudConfig.put("COMPONENT_TYPE", component.getComponentType());
        cloudConfig.put("LOG_PATH", "/var/log/kvm-cloud.log");
        cloudConfig.put("LOG_BACKUP_DAYS", 7);

        List<GuestNetworkEntity> guestNetworkEntityList = this.guestNetworkDao.listByAllocate(Constant.NetworkAllocateType.GUEST, guest.getGuestId());
        for (GuestNetworkEntity guestNetworkEntity : guestNetworkEntityList) {
            if (Objects.equals(guestNetworkEntity.getNetworkId(), network.getNetworkId())) {
                cloudConfig.put("INTERNAL_INTERFACE", "eth" + guestNetworkEntity.getDeviceId());
                cloudConfig.put("INTERNAL_IP", guestNetworkEntity.getIp());
                break;
            }
        }
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);
        String yamlStr = yaml.dump(cloudConfig);
        config.appendFile("/etc/kvm-cloud/config.yaml", yamlStr);
        config.appendRuncmd("systemctl enable kvm-cloud");
        config.appendRuncmd("systemctl restart kvm-cloud");
        String systemType = this.configService.getConfig(ConfigKey.SYSTEM_COMPONENT_SYSTEM_TYPE, "centos");
        if ("centos".equalsIgnoreCase(systemType)) {
            config.appendRuncmd("iptables-save > /etc/sysconfig/iptables");
        } else if ("ubuntu".equalsIgnoreCase(systemType)) {
            config.appendRuncmd("netfilter-persistent save");
        }
        config.appendRuncmd("systemctl restart iptables");
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
