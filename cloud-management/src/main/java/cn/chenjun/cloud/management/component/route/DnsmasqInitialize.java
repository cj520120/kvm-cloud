package cn.chenjun.cloud.management.component.route;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.GuestNetworkMapper;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.IpCalculate;
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
    private final int MIN_DHCP_SIZE = 2;

    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component, int guestId) {
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();

        NetworkEntity network = this.networkMapper.selectById(component.getNetworkId());
        if (network == null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "Dnsmasq初始化失败.组件所属网络不存在,ComponentId=" + component.getComponentId());
        }
        List<GuestNetworkEntity> allGuestNetwork = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_ID, component.getNetworkId()));
        Optional<GuestNetworkEntity> optional = allGuestNetwork.stream().filter(t -> Objects.equals(t.getAllocateId(), guestId) && Objects.equals(t.getAllocateType(), Constant.NetworkAllocateType.GUEST)).findFirst();
        if (!optional.isPresent()) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "Dnsmasq初始化失败.未找到监听网卡,ComponentId=" + component.getComponentId() + ",GuestId=" + guestId);
        }
        GuestNetworkEntity defaultGuestNetwork = optional.get();
        //删除VIP地址、网关地址、以及本机的IP地址
        allGuestNetwork.removeIf(t -> Objects.equals(t.getIp(), network.getGateway()) || Objects.equals(t.getIp(), component.getComponentVip()) || (Objects.equals(t.getAllocateId(), guestId) && Objects.equals(t.getAllocateType(), Constant.NetworkAllocateType.GUEST)));
        allGuestNetwork.sort(Comparator.comparingLong(o -> IpCalculate.ipToLong(o.getIp())));
        if (allGuestNetwork.size() < MIN_DHCP_SIZE) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "Dnsmasq初始化失败.没有可分配的区间,ComponentId=" + component.getComponentId());
        }
        String startIp = allGuestNetwork.get(0).getIp();
        String endIp = allGuestNetwork.get(allGuestNetwork.size() - 1).getIp();

        String config = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/route/dnsmasq.tpl")), StandardCharsets.UTF_8);
        Jinjava jinjava = new Jinjava();
        Map<String, Object> map = new HashMap<>(0);
        map.put("interface", "eth" + defaultGuestNetwork.getDeviceId());
        map.put("ip", defaultGuestNetwork.getIp());
        map.put("vip", component.getComponentVip());
        map.put("startIp", startIp);
        map.put("endIp", endIp);
        if(network.getBasicNetworkId()>0) {
            map.put("gateway", component.getComponentVip());
        }else{
            map.put("gateway", network.getGateway());
        }
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
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("sh").args(new String[]{"/tmp/check_install_service_shell.sh", "dnsmasq"}).build())).build());
        //写入dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/dnsmasq.conf").fileBody(dnsmasqConfig).build())).build());
        //创建hosts文件
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("mkdir").args(new String[]{"-p", "/etc/dnsmasq.hosts.d"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("chmod").args(new String[]{"755", "/etc/dnsmasq.hosts.d"}).build())).build());
        //启动dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("touch").args(new String[]{"/etc/dnsmasq.hosts.d/hosts"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("chmod").args(new String[]{"644", "/etc/dnsmasq.hosts.d/hosts"}).build())).build());
        //启动dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "dnsmasq"}).build())).build());

        //写入dns自动加载配置
        return commands;
    }

    @Override
    public int getOrder() {
        return ComponentOrder.DNS;
    }
}
