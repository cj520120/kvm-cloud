package cn.roamblue.cloud.management.component;

import cn.roamblue.cloud.common.bean.GuestQmaRequest;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.ComponentEntity;
import cn.roamblue.cloud.management.data.entity.GuestEntity;
import cn.roamblue.cloud.management.data.entity.GuestNetworkEntity;
import cn.roamblue.cloud.management.data.entity.NetworkEntity;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class RouteService extends AbstractComponentService {
    @Override
    public int getComponentType() {
        return Constant.ComponentType.ROUTE;
    }

    @Override
    public String getComponentName() {
        return "System Route";
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void create(int networkId) {
        ComponentEntity component = this.componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq("component_type", Constant.ComponentType.VNC).eq("network_id", networkId).last("limit 0 ,1"));
        if (component == null) {
            return;
        }
        GuestEntity vncGuest = this.guestMapper.selectById(component.getGuestId());
        if (vncGuest == null || !Objects.equals(vncGuest.getStatus(), Constant.GuestStatus.RUNNING)) {
            return;
        }
        super.create(networkId);
    }

    @Override
    public GuestQmaRequest getQmaRequest(int guestId) {
        ComponentEntity component = this.componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq("guest_id", guestId));
        if (component == null) {
            return null;
        }
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        GuestQmaRequest request = GuestQmaRequest.builder().build();
        request.setName("");
        request.setTimeout((int) TimeUnit.MINUTES.toSeconds(5));
        request.setCommands(commands);
        //写入默认网卡
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/sysconfig/network-scripts/ifcfg-eth0").fileBody(this.getNicConfig(0, "169.254.254.254", null, null, null)).build())).build());
        List<GuestNetworkEntity> guestNetworkList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", guestId));
        GuestNetworkEntity defaultGuestNetwork = null;
        NetworkEntity defaultNetwork = this.networkMapper.selectById(component.getNetworkId());
        int startNetworkDeviceId = 1;
        if (Objects.equals(defaultNetwork.getType(), Constant.NetworkType.VLAN)) {
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/sysconfig/network-scripts/ifcfg-eth1").fileBody(this.getNicConfig(1, defaultNetwork.getGateway(), defaultNetwork.getMask(), defaultNetwork.getGateway(), defaultNetwork.getDns())).build())).build());
            startNetworkDeviceId = 2;
        }
        for (int i = 0; i < guestNetworkList.size(); i++) {
            GuestNetworkEntity guestNetwork = guestNetworkList.get(i);
            NetworkEntity network = this.networkMapper.selectById(guestNetwork.getNetworkId());
            int index = guestNetwork.getDeviceId() + startNetworkDeviceId;

            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/sysconfig/network-scripts/ifcfg-eth" + index).fileBody(this.getNicConfig(index, guestNetwork.getIp(), network.getMask(), network.getGateway(), network.getDns())).build())).build());
            if (Objects.equals(network.getNetworkId(), component.getNetworkId())) {
                defaultGuestNetwork = guestNetwork;
            }
        }
        //重启网卡
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "network"}).build())).build());
        StringBuilder dhcp = new StringBuilder();
        dhcp.append("interface=eth").append(defaultGuestNetwork.getDeviceId() + 1).append("\r\n");
        dhcp.append("listen-address=").append(defaultGuestNetwork.getIp()).append("\r\n");
        dhcp.append("no-dhcp-interface=").append("\r\n");
        dhcp.append("dhcp-range=").append(defaultNetwork.getStartIp()).append(",").append(defaultNetwork.getEndIp()).append(",").append("24h").append("\r\n");
        List<GuestNetworkEntity> allGuestNetwork = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("network_id", component.getNetworkId()));
        for (GuestNetworkEntity guestNetwork : allGuestNetwork) {
            //写入网卡
            dhcp.append("dhcp-host=").append(guestNetwork.getMac()).append(",").append(guestNetwork.getIp()).append("\r\n");
        }
        dhcp.append("dhcp-option=option:router,").append(defaultNetwork.getGateway()).append("\r\n");
        dhcp.append("dhcp-option=option:netmask,").append(defaultNetwork.getMask()).append("\r\n");
        dhcp.append("dhcp-option=option:dns-server,").append(defaultNetwork.getDns()).append("\r\n");
        dhcp.append("dhcp-option=option:classless-static-route,169.254.254.254/32,").append(defaultGuestNetwork.getIp()).append("\r\n");
        //下载dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"install", "-y", "dnsmasq"}).build())).build());
        //写入dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/dnsmasq.conf").fileBody(dhcp.toString()).build())).build());
        //启动dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"enable", "dnsmasq"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "dnsmasq"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("hostnamectl").args(new String[]{"set-hostname", this.getComponentName()}).build())).build());
        return request;
    }
}
