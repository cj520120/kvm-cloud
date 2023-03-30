package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.GuestNetworkMapper;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@ConditionalOnProperty(name = "app.route.type", havingValue = "dhcp", matchIfMissing = true)
public class DhcpInitializeService implements RouteInitialize {
    @Autowired
    protected GuestNetworkMapper guestNetworkMapper;
    @Autowired
    protected NetworkMapper networkMapper;
    @Autowired
    protected GuestMapper guestMapper;

    @Override
    public List<GuestQmaRequest.QmaBody> initialize(int guestId) {
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest == null) {
            return commands;
        }
        NetworkEntity network = this.networkMapper.selectById(guest.getNetworkId());
        if (network == null) {
            return commands;
        }
        StringBuilder dhcp = new StringBuilder();
        dhcp.append("ddns-update-style none;\r\n").append("ignore client-updates;\r\n");
        dhcp.append("default-lease-time 86400;\r\n");
        dhcp.append("max-lease-time 172800;\r\n");
        dhcp.append(String.format("option domain-name-servers %s;\r\n", network.getDns()));
        dhcp.append(String.format("subnet %s netmask %s {\r\n", network.getSubnet(), network.getMask()));
        dhcp.append(String.format("  range %s %s;\r\n", network.getStartIp(), network.getEndIp()));
        dhcp.append(String.format("  option routers %s;\r\n", network.getGateway()));
        dhcp.append(String.format("  option broadcast-address %s;\r\n", network.getBroadcast()));
        dhcp.append("  group{\r\n");
        List<GuestNetworkEntity> allGuestNetwork = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("network_id", guest.getNetworkId()));
        for (GuestNetworkEntity guestNetworkEntity : allGuestNetwork) {
            dhcp.append(String.format("    host vm-network-%d{\r\n", guestNetworkEntity.getGuestNetworkId()));
            dhcp.append(String.format("       hardware ethernet %s;\r\n", guestNetworkEntity.getMac()));
            dhcp.append(String.format("       fixed-address %s;\r\n", guestNetworkEntity.getIp()));
            dhcp.append("    }\r\n");
        }
        dhcp.append("  }\r\n");
        dhcp.append("}");
        //下载dhcp
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"install", "-y", "dhcp"}).checkSuccess(true).build())).build());
        //写入dhcp
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/dhcp/dhcpd.conf").fileBody(dhcp.toString()).build())).build());
        //启动dhcp
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"enable", "dhcpd"}).checkSuccess(true).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "dhcpd"}).checkSuccess(true).build())).build());
        return commands;

    }
}
