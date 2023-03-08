package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.IpCaculate;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
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
        int startNetworkDeviceId = 0;
        List<GuestNetworkEntity> guestNetworkList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", guestId));
        NetworkEntity defaultNetwork = this.networkMapper.selectById(component.getNetworkId());
        if (Objects.equals(defaultNetwork.getType(), Constant.NetworkType.VLAN)) {
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/sysconfig/network-scripts/ifcfg-eth" + startNetworkDeviceId).fileBody(this.getNicConfig(1, defaultNetwork.getGateway(), defaultNetwork.getMask(), defaultNetwork.getGateway(), defaultNetwork.getDns())).build())).build());
            startNetworkDeviceId = 2;
        }
        for (int i = 0; i < guestNetworkList.size(); i++) {
            GuestNetworkEntity guestNetwork = guestNetworkList.get(i);
            NetworkEntity network = this.networkMapper.selectById(guestNetwork.getNetworkId());
            int index = guestNetwork.getDeviceId() + startNetworkDeviceId;

            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/sysconfig/network-scripts/ifcfg-eth" + index).fileBody(this.getNicConfig(index, guestNetwork.getIp(), network.getMask(), network.getGateway(), network.getDns())).build())).build());

        }

        //重启网卡
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "network"}).build())).build());
        StringBuilder dhcp = new StringBuilder();
        dhcp.append("ddns-update-style none;\r\n").append("ignore client-updates;\r\n");
        dhcp.append(String.format("subnet %s netmask %s {\r\n", IpCaculate.getSubnetByIp(defaultNetwork.getStartIp()), defaultNetwork.getMask()));
        dhcp.append(String.format("  range %s %s;\r\n", defaultNetwork.getStartIp(), defaultNetwork.getEndIp()));
        dhcp.append(String.format("  option routers %s;\r\n", defaultNetwork.getGateway()));
        dhcp.append(String.format("  option broadcast-address %s;\r\n", IpCaculate.getBroadcastByIp(defaultNetwork.getStartIp())));
        dhcp.append("  default-lease-time 600;\r\n");
        dhcp.append("  max-lease-time 7200;\r\n");
        dhcp.append(String.format("  option domain-name-servers %s;\r\n", defaultNetwork.getDns()));
        dhcp.append("  group{\r\n");
        List<GuestNetworkEntity> allGuestNetwork = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("network_id", component.getNetworkId()));
        for (GuestNetworkEntity guestNetworkEntity : allGuestNetwork) {

            dhcp.append(String.format("    host vm-network-%d{\r\n", guestNetworkEntity.getGuestNetworkId()));
            dhcp.append(String.format("       hardware ethernet %s;\r\n", guestNetworkEntity.getMac()));
            dhcp.append(String.format("       fixed-address %s;\r\n", guestNetworkEntity.getIp()));
            dhcp.append("    }\r\n");
        }
        dhcp.append("  }\r\n");
        dhcp.append("}");
        //下载dhcp
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"install", "-y", "dhcp"}).build())).build());
        //写入dhcp
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/dhcp/dhcpd.conf").fileBody(dhcp.toString()).build())).build());
        //启动dhcp
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"enable", "dhcpd"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "dhcpd"}).build())).build());

        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("hostnamectl").args(new String[]{"set-hostname", this.getComponentName()}).build())).build());
        return request;
    }
}
