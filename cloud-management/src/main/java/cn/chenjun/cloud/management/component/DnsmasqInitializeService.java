package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.GuestNetworkMapper;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.hutool.core.io.resource.ResourceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hubspot.jinjava.Jinjava;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author chenjun
 */
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
        List<GuestNetworkEntity> allGuestNetwork = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("network_id", guest.getNetworkId()));

        String config = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/dnsmasq.tpl")), StandardCharsets.UTF_8);
        Jinjava jinjava = new Jinjava();
        Map<String, Object> map = new HashMap<>(0);
        map.put("ip", defaultGuestNetwork.getIp());
        map.put("startIp", network.getStartIp());
        map.put("endIp", network.getEndIp());
        map.put("gateway", network.getGateway());
        map.put("mask", network.getMask());
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
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("mkdir").args(new String[]{"/etc/dnsmasq.hosts.d"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("chmod").args(new String[]{"755","/etc/dnsmasq.hosts.d"}).build())).build());
        //启动dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("touch").args(new String[]{"/etc/dnsmasq.hosts.d/hosts"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("chmod").args(new String[]{"644","/etc/dnsmasq.hosts.d/hosts"}).build())).build());
        //启动dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"enable", "dnsmasq"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "dnsmasq"}).build())).build());
        return commands;

    }

    @Override
    public boolean isEnableMetaService() {
        return true;
    }
}
