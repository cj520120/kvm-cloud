package cn.chenjun.cloud.management.component.route;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.GuestNetworkMapper;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class NetworkInitialize implements RouteComponentQmaInitialize {
    @Autowired
    private GuestNetworkMapper guestNetworkMapper;
    @Autowired
    private NetworkMapper networkMapper;

    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component) {
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        //写入网卡固定IP
        List<GuestNetworkEntity> guestNetworkList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", component.getGuestId()));
        guestNetworkList.sort(Comparator.comparingInt(GuestNetworkEntity::getDeviceId));
        String[] iptablesRules = null;
        for (int i = 0; i < guestNetworkList.size(); i++) {
            GuestNetworkEntity guestNetwork = guestNetworkList.get(i);
            NetworkEntity network = this.networkMapper.selectById(guestNetwork.getNetworkId());
            int index = guestNetwork.getDeviceId();
            String networkConfig;
            if (network.getType().equals(Constant.NetworkType.BASIC)) {
                iptablesRules = new String[]{"-t", "nat", "-A", "POSTROUTING", "-o", "eth" + index, "-j", "MASQUERADE"};
                networkConfig = this.getNicConfig(index, guestNetwork.getIp(), network.getMask(), network.getGateway(), network.getDns());
            } else {
                networkConfig = this.getNicConfig(index, guestNetwork.getIp(), network.getMask(), "", "");

            }
            if (i == 0) {
                networkConfig += "\r\nIPADDR1=169.254.169.254";
            }
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/sysconfig/network-scripts/ifcfg-eth" + index).fileBody(networkConfig).build())).build());
        }
        //重启网卡
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "network"}).checkSuccess(true).build())).build());
        if (iptablesRules != null) {
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("iptables").args(iptablesRules).checkSuccess(true).build())).build());
        }
        return commands;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    protected String getNicConfig(int index, String ip, String netmask, String gateway, String dns) {
        StringBuilder sb = new StringBuilder();
        sb.append("TYPE=Ethernet").append("\r\n");
        sb.append("BROWSER_ONLY=no").append("\r\n");
        sb.append("BOOTPROTO=static").append("\r\n");
        sb.append("DEFROUTE=yes").append("\r\n");
        sb.append("IPV4_FAILURE_FATAL=no").append("\r\n");
        sb.append("NAME=eth").append(index).append("\r\n");
        sb.append("DEVICE=eth").append(index).append("\r\n");
        sb.append("ONBOOT=yes").append("\r\n");
        if (!StringUtils.isEmpty(ip)) {
            sb.append("IPADDR=").append(ip).append("\r\n");
        }
        if (!StringUtils.isEmpty(netmask)) {
            sb.append("NETMASK=").append(netmask).append("\r\n");
        }
        if (!StringUtils.isEmpty(gateway)) {
            sb.append("GATEWAY=").append(gateway).append("\r\n");
        }
        if (!StringUtils.isEmpty(dns)) {
            for (String s : dns.split(",")) {
                String dnsStr = s.trim();
                if (!StringUtils.isEmpty(dnsStr)) {
                    sb.append("DNS1=").append(dnsStr).append("\r\n");
                }
            }
        }
        return sb.toString();
    }
}
