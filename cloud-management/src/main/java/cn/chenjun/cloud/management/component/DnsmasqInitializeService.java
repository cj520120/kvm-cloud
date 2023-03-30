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
@ConditionalOnProperty(name = "app.route.type", havingValue = "dnsmasq")
public class DnsmasqInitializeService implements RouteInitialize {
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
        GuestNetworkEntity defaultGuestNetwork = this.guestNetworkMapper.selectOne(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", guestId).eq("network_id", guest.getNetworkId()).eq("device_id", 0));
        if (defaultGuestNetwork == null) {
            return commands;
        }
        NetworkEntity network = this.networkMapper.selectById(guest.getNetworkId());
        if (network == null) {
            return commands;
        }
        StringBuilder dhcp = new StringBuilder();
        dhcp.append("interface=eth0").append("\r\n");
        dhcp.append("listen-address=").append(defaultGuestNetwork.getIp()).append("\r\n");
        dhcp.append("no-dhcp-interface=").append("\r\n");
        dhcp.append("dhcp-range=").append(network.getStartIp()).append(",").append(network.getEndIp()).append(",").append("48h").append("\r\n");
        List<GuestNetworkEntity> allGuestNetwork = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("network_id", guest.getNetworkId()));
        for (GuestNetworkEntity guestNetwork : allGuestNetwork) {
            //写入网卡
            dhcp.append("dhcp-host=").append(guestNetwork.getMac()).append(",").append(guestNetwork.getIp()).append("\r\n");
        }
        dhcp.append("dhcp-option=option:router,").append(network.getGateway()).append("\r\n");
        dhcp.append("dhcp-option=option:netmask,").append(network.getMask()).append("\r\n");
        dhcp.append("dhcp-option=option:dns-server,").append(network.getDns()).append("\r\n");
        dhcp.append("dhcp-option=option:classless-static-route,169.254.169.254/32,").append(defaultGuestNetwork.getIp()).append(",0.0.0.0/0,").append(network.getGateway()).append("\r\n");
        //下载dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"install", "-y", "dnsmasq"}).build())).build());
        //写入dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/dnsmasq.conf").fileBody(dhcp.toString()).build())).build());
        //启动dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"enable", "dnsmasq"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "dnsmasq"}).build())).build());
        return commands;

    }
}
