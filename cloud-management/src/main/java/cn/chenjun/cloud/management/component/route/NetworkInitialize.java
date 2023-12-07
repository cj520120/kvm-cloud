package cn.chenjun.cloud.management.component.route;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.GuestNetworkMapper;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.util.Constant;
import cn.hutool.core.io.resource.ResourceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hubspot.jinjava.Jinjava;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class NetworkInitialize implements RouteComponentQmaInitialize {
    @Autowired
    private GuestNetworkMapper guestNetworkMapper;
    @Autowired
    private NetworkMapper networkMapper;

    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component, int guestId) {
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        //写入网卡固定IP
        List<GuestNetworkEntity> guestNetworkList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("allocate_id", guestId).eq("allocate_type", Constant.NetworkAllocateType.GUEST));
        guestNetworkList.sort(Comparator.comparingInt(GuestNetworkEntity::getDeviceId));
        String[] iptablesRules = null;
        for (GuestNetworkEntity guestNetwork : guestNetworkList) {
            NetworkEntity network = this.networkMapper.selectById(guestNetwork.getNetworkId());
            int index = guestNetwork.getDeviceId();
            String networkConfig;
            if (network.getType().equals(Constant.NetworkType.BASIC)) {
                iptablesRules = new String[]{"-t", "nat", "-A", "POSTROUTING", "-o", "eth" + index, "-j", "MASQUERADE"};
                networkConfig = this.getNicConfig(index, guestNetwork.getIp(), network.getMask(), network.getGateway(), network.getDns());
            } else {
                networkConfig = this.getNicConfig(index, guestNetwork.getIp(), network.getMask(), "", "");

            }
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/sysconfig/network-scripts/ifcfg-eth" + index).fileBody(networkConfig).build())).build());
        }
        //重启网卡
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("service").args(new String[]{"network", "restart"}).checkSuccess(true).build())).build());
        if (iptablesRules != null) {
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("iptables").args(iptablesRules).checkSuccess(true).build())).build());
        }
        return commands;
    }

    @Override
    public int getOrder() {
        return RouteOrder.NETWORK;
    }

    protected String getNicConfig(int index, String ip, String netmask, String gateway, String dns) {
        String body =   new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/network.tpl")), StandardCharsets.UTF_8);
        Jinjava jinjava = new Jinjava();
        Map<String, Object> map = new HashMap<>(5);
        map.put("index",index);
        map.put("ip",ip);
        map.put("gateway",gateway);
        map.put("netmask",netmask);
        List<String> dnsList=Arrays.stream(dns.split(",")).filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        map.put("dnsList",dnsList);
        return jinjava.render(body, map).replaceAll("(?m)^[ \t]*\r?\n", "");
    }
}
