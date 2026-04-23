package cn.chenjun.cloud.management.operate.impl.cloud.impl.component.impl.global;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.MapUtil;
import cn.chenjun.cloud.common.util.ResourceUtil;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.operate.impl.cloud.bean.CloudConfig;
import cn.chenjun.cloud.management.operate.impl.cloud.bean.SystemParamConfig;
import cn.chenjun.cloud.management.operate.impl.cloud.impl.component.impl.BaseInitialization;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.KeepalivedConfigGenerator;
import cn.chenjun.cloud.management.util.SubnetCalculator;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class FirstInitialization extends BaseInitialization {


    @Override
    public boolean isSupport(int componentType) {
        return true;
    }

    @Override
    public void initialize(CloudConfig config, GuestEntity guest, NetworkEntity network, ComponentEntity component, ComponentGuestEntity componentGuest) {
        config.addMetaData("instance-id", guest.getName());
        config.addMetaData("hostname", Constant.ComponentType.getComponentName(component.getComponentType()));
        config.addMetaData("local-hostname", Constant.ComponentType.getComponentName(component.getComponentType()));
        config.appendPackage("iptables");
        String systemType = this.configService.getConfig(ConfigKey.SYSTEM_COMPONENT_SYSTEM_TYPE, "centos");
        if ("centos".equalsIgnoreCase(systemType)) {
            config.appendPackage("iptables-services");
        } else if ("ubuntu".equalsIgnoreCase(systemType)) {
            config.appendPackage("iptables-persistent");
        }
        config.appendPackage("python3");
        config.appendPackage("python3-pip");
        config.appendPackage("qemu-guest-agent");
        ResourceUtil.listResources("tpl/component/init/kvm-cloud").forEach(resourceContent -> {
            String toPath = "/usr/local/kvm-cloud" + resourceContent.getPath() + "/" + resourceContent.getFilename();
            config.appendFileB64(toPath, resourceContent.getContent());
        });
        config.appendResourceFile("/etc/systemd/system/kvm-cloud.service", "tpl/component/init/kvm-cloud.service.json");
        config.appendRuncmd("mkdir -p /usr/local/kvm-cloud");
        config.appendRuncmd("python3 -m venv /usr/local/kvm-cloud/venv");
        config.appendRuncmd("/usr/local/kvm-cloud/venv/bin/python -m pip install --upgrade pip");
        config.appendRuncmd("/usr/local/kvm-cloud/venv/bin/pip install -r /usr/local/kvm-cloud/requirements.txt");
        if ("centos".equalsIgnoreCase(systemType)) {
            config.appendFile("/etc/sysconfig/qemu-ga", "BLACKLIST_RPC=");
        }
        config.appendRuncmd("systemctl enable qemu-guest-agent");
        config.appendRuncmd("systemctl restart qemu-guest-agent");
        String userName = this.configService.getConfig(ConfigKey.SYSTEM_COMPONENT_USER_NAME, "kvm-user");
        String password = this.configService.getConfig(ConfigKey.SYSTEM_COMPONENT_USER_PASSWORD, "Kvm@123456");
        config.appendUserData("chpasswd", MapUtil.of("expire", false));
        config.appendUserData("ssh_pwauth", true);
        config.appendUserData("disable_root", false);
        config.appendUserData("timezone", "Etc/UTC");
        config.appendUserData("manage_etc_hosts", true);
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("name", userName);
        user.put("groups", Arrays.asList("wheel", "sudo"));
        user.put("shell", "/bin/bash");
        user.put("lock_passwd", false);
        user.put("plain_text_passwd", password);
        user.put("sudo", Collections.singletonList("ALL=(ALL) NOPASSWD: ALL"));
        config.addUser("default");
        config.addUser(user);
        List<GuestNetworkEntity> guestNicList = this.guestNetworkDao.listByAllocate(Constant.NetworkAllocateType.GUEST, guest.getGuestId());
        for (int i = 0; i < guestNicList.size(); i++) {
            GuestNetworkEntity nic = guestNicList.get(i);
            Map<String, Object> nicConfig = new HashMap<>();
            nicConfig.put("match", MapUtil.of("macaddress", nic.getMac().toLowerCase()));
            nicConfig.put("dhcp4", false);
            nicConfig.put("dhcp6", false);
            NetworkEntity nicNetwork = network;
            if (!Objects.equals(nic.getNetworkId(), network.getNetworkId())) {
                nicNetwork = networkDao.findById(nic.getNetworkId());
            }
            nicConfig.put("addresses", Collections.singletonList(nic.getIp() + "/" + Long.bitCount(SubnetCalculator.ipToLong(nicNetwork.getMask()))));
            if (i == 0) {
                nicConfig.put("routes", Collections.singletonList(
                        MapUtil.of("to", "0.0.0.0/0", "via", nicNetwork.getGateway())
                ));
            }
            nicConfig.put("nameservers", MapUtil.of("addresses", Arrays.asList(nicNetwork.getDns().split(","))));
            nicConfig.put("set-name", "eth" + i);
            config.addNetwork("eth" + i, nicConfig);
        }
        String sysctlConfig = ResourceUtil.readUtf8Str("tpl/component/init/sysctl.config.json");
        List<SystemParamConfig> sysctlConfigList = GsonBuilderUtil.create().fromJson(sysctlConfig, new TypeToken<List<SystemParamConfig>>() {
        }.getType());
        for (SystemParamConfig sysctl : sysctlConfigList) {
            if (sysctl.getSupportNetworks().contains(network.getType())) {
                Map<String, Integer> params = sysctl.getParams();
                params.forEach((k, v) -> {
                    config.appendRuncmd(String.format("echo '%s=%d' >> /etc/sysctl.conf", k, v));
                });
            }
        }
        config.appendRuncmd("sysctl -p");
        config.appendRuncmd("setenforce 0");
        config.appendRuncmd("sed -i 's/^SELINUX=.*/SELINUX=disabled/' /etc/selinux/config");
        config.appendRuncmd("systemctl stop firewalld || true");
        config.appendRuncmd("systemctl disable firewalld || true");
        config.appendRuncmd("systemctl enable iptables");
        config.appendRuncmd("systemctl start iptables");
        config.appendRuncmd("iptables -F");
        config.appendRuncmd("iptables -t nat -F");
        config.appendRuncmd("iptables -A INPUT -p vrrp -j ACCEPT");
        config.appendRuncmd(String.format("iptables -A INPUT -d %s -j ACCEPT", KeepalivedConfigGenerator.generateMcastGroup4(network.getPoolId(), component.getComponentId())));
        config.appendRuncmd("iptables -A INPUT -p tcp --dport 22 -j ACCEPT");
        guestNicList.sort(Comparator.comparing(GuestNetworkEntity::getDeviceId));
        for (int i = 0; i < guestNicList.size(); i++) {
            GuestNetworkEntity nic = guestNicList.get(i);
            if (i == 0) {
                config.appendRuncmd("iptables -t nat -A POSTROUTING -o eth0 -s " + network.getSubnet() + "/" + network.getMask() + " -j MASQUERADE");
            } else {
                config.appendRuncmd("iptables -A FORWARD -i eth" + nic.getDeviceId() + " -o eth0 -j ACCEPT");
                config.appendRuncmd("iptables -A FORWARD -i eth0 -o eth" + nic.getDeviceId() + " -m state --state RELATED,ESTABLISHED -j ACCEPT");
            }
        }
        if (network.getBasicNetworkId() > 0) {
            NetworkEntity basicNetwork = networkDao.findById(network.getBasicNetworkId());
            String snat = String.format("iptables -t nat -A POSTROUTING -s %s/%s -d %s/%s -j SNAT --to-source %s", basicNetwork.getSubnet(), basicNetwork.getMask(), network.getSubnet(), network.getMask(), guest.getGuestIp());
            config.appendRuncmd(snat);
        }
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
