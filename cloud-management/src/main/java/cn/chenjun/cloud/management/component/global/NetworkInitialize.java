package cn.chenjun.cloud.management.component.global;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.component.route.ComponentOrder;
import cn.chenjun.cloud.management.config.ApplicationConfig;
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

/**
 * @author chenjun
 */
@Component
public class NetworkInitialize implements GlobalComponentQmaInitialize {
    @Autowired
    private GuestNetworkMapper guestNetworkMapper;
    @Autowired
    private NetworkMapper networkMapper;
    @Autowired
    private ApplicationConfig applicationConfig;

    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component, int guestId) {
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        //写入网卡固定IP
        List<GuestNetworkEntity> guestNetworkList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.ALLOCATE_ID, guestId).eq(GuestNetworkEntity.ALLOCATE_TYPE, Constant.NetworkAllocateType.GUEST));
        guestNetworkList.sort(Comparator.comparingInt(GuestNetworkEntity::getDeviceId));

        List<String> routeCommands = new ArrayList<>();
        for (GuestNetworkEntity guestNetwork : guestNetworkList) {
            NetworkEntity network = this.networkMapper.selectById(guestNetwork.getNetworkId());
            int index = guestNetwork.getDeviceId();
            String networkConfig;
            List<String> otherIpList = null;
            if (Objects.equals(network.getNetworkId(), component.getNetworkId())) {
                otherIpList = Collections.singletonList("169.254.169.254");
            }
            if (network.getType().equals(Constant.NetworkType.BASIC)) {
                networkConfig = this.getNicConfig(index, guestNetwork.getIp(), network.getMask(), network.getGateway(), network.getDns(), otherIpList);
            } else {
                networkConfig = this.getNicConfig(index, guestNetwork.getIp(), network.getMask(), network.getGateway(), network.getDns(), otherIpList);
                //删除vlan网卡的外网转发
                routeCommands.add(String.format("route del -net 0.0.0.0 netmask 0.0.0.0 dev eth%d", index));
            }
            routeCommands.add(String.format("iptables -t nat -A POSTROUTING -o eth%d -j MASQUERADE ", index));
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/sysconfig/network-scripts/ifcfg-eth" + index).fileBody(networkConfig).build())).build());
        }
        //重启网卡
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("service").args(new String[]{"NetworkManager", "restart"}).checkSuccess(true).build())).build());
        //安装网络检测脚本
        String networkCheckScript = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/script/network_check_shell.tpl")), StandardCharsets.UTF_8);
        Jinjava jinjava = new Jinjava();
        Map<String, Object> map = new HashMap<>(1);
        map.put("commands", routeCommands);
        if (ObjectUtils.isEmpty(this.applicationConfig.getNetworkCheckAddress())) {
            map.put("check-address", this.applicationConfig.getNetworkCheckAddress());
        } else {
            map.put("check-address", "8.8.8.8");
        }
        networkCheckScript = jinjava.render(networkCheckScript, map);
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/tmp/network_check.sh").fileBody(networkCheckScript).build())).build());


        //检测网卡初始化完成
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("sh").args(new String[]{"/tmp/network_check.sh"}).checkSuccess(true).build())).build());

        return commands;
    }

    @Override
    public int getOrder() {
        return ComponentOrder.NETWORK;
    }

    protected String getNicConfig(int index, String ip, String netmask, String gateway, String dns, List<String> otherIpList) {
        String body = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/network/network.tpl")), StandardCharsets.UTF_8);
        Jinjava jinjava = new Jinjava();
        Map<String, Object> map = new HashMap<>(5);
        map.put("index", index);
        map.put("ip", ip);
        map.put("gateway", gateway);
        map.put("netmask", netmask);
        map.put("otherIpList", otherIpList);
        List<String> dnsList = Arrays.stream(dns.split(",")).filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        map.put("dnsList", dnsList);
        return jinjava.render(body, map).replaceAll("(?m)^[ \t]*\r?\n", "");
    }
}
