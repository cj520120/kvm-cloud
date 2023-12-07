package cn.chenjun.cloud.management.component.route;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.GuestNetworkMapper;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.util.Constant;
import cn.hutool.core.io.resource.ResourceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hubspot.jinjava.Jinjava;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
public class DnsmasqInitialize implements RouteComponentQmaInitialize {
    @Autowired
    protected GuestNetworkMapper guestNetworkMapper;
    @Autowired
    protected NetworkMapper networkMapper;
    @Autowired
    protected GuestMapper guestMapper;

    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component, int guestId) {
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        GuestNetworkEntity defaultGuestNetwork = this.guestNetworkMapper.selectOne(new QueryWrapper<GuestNetworkEntity>().eq("allocate_id", guestId).eq("allocate_type", Constant.NetworkAllocateType.GUEST).eq("network_id", component.getNetworkId()).eq("device_id", 0));
        if (defaultGuestNetwork == null) {
            return commands;
        }
        NetworkEntity network = this.networkMapper.selectById(component.getNetworkId());
        if (network == null) {
            return commands;
        }
        List<GuestNetworkEntity> allGuestNetwork = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("network_id", component.getNetworkId()));
        String config = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/dnsmasq.tpl")), StandardCharsets.UTF_8);
        Jinjava jinjava = new Jinjava();
        Map<String, Object> map = new HashMap<>(0);
        map.put("ip", defaultGuestNetwork.getIp());
        map.put("vip", component.getComponentVip());
        map.put("startIp", network.getStartIp());
        map.put("endIp", network.getEndIp());
        map.put("gateway", component.getComponentVip());
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
        String dnsmasqConfig = jinjava.render(config, map);
        //下载dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"install", "-y", "dnsmasq"}).build())).build());
        //写入dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/dnsmasq.conf").fileBody(dnsmasqConfig).build())).build());
        //创建hosts文件
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("mkdir").args(new String[]{"-p", "/etc/dnsmasq.hosts.d"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("chmod").args(new String[]{"755", "/etc/dnsmasq.hosts.d"}).build())).build());
        //启动dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("touch").args(new String[]{"/etc/dnsmasq.hosts.d/hosts"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("chmod").args(new String[]{"644", "/etc/dnsmasq.hosts.d/hosts"}).build())).build());
        //启动dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"enable", "dnsmasq"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "dnsmasq"}).build())).build());

        //写入dns自动加载配置
        return commands;
    }

    @Override
    public int getOrder() {
        return RouteOrder.DNS;
    }
}
