package cn.chenjun.cloud.management;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.hutool.core.io.resource.ResourceUtil;
import com.google.common.reflect.TypeToken;
import com.hubspot.jinjava.Jinjava;
import org.apache.commons.lang3.ObjectUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


public class TplTest {
    public static void main(String[] args) {
        testNetwork();
    }

    private static void testDnsmasq() {
        NetworkEntity network= GsonBuilderUtil.create().fromJson("{\"networkId\":17,\"name\":\"default\",\"startIp\":\"192.168.1.210\",\"endIp\":\"192.168.1.230\",\"gateway\":\"192.168.1.1\",\"mask\":\"255.255.255.0\",\"subnet\":\"192.168.1.0\",\"broadcast\":\"192.168.1.2\",\"bridge\":\"br0\",\"dns\":\"192.168.1.2,8.8.8.8,114.114.114.114\",\"type\":0,\"status\":2,\"vlanId\":0,\"secret\":\"CJ:KVM:CLOUD\",\"basicNetworkId\":0,\"createTime\":1679612040000}", NetworkEntity.class);
        List<GuestNetworkEntity> allGuestNetwork = GsonBuilderUtil.create().fromJson("[{\"guestNetworkId\":490,\"guestId\":176,\"networkId\":17,\"deviceId\":0,\"driveType\":\"virtio\",\"mac\":\"00:01:43:41:B2:61\",\"ip\":\"192.168.1.210\",\"createTime\":1679612040000},{\"guestNetworkId\":491,\"guestId\":177,\"networkId\":17,\"deviceId\":1,\"driveType\":\"virtio\",\"mac\":\"00:D0:BC:A1:35:55\",\"ip\":\"192.168.1.211\",\"createTime\":1679612040000},{\"guestNetworkId\":492,\"guestId\":178,\"networkId\":17,\"deviceId\":1,\"driveType\":\"virtio\",\"mac\":\"00:90:8E:E2:71:18\",\"ip\":\"192.168.1.212\",\"createTime\":1679612040000},{\"guestNetworkId\":493,\"guestId\":179,\"networkId\":17,\"deviceId\":0,\"driveType\":\"virtio\",\"mac\":\"00:1F:9E:A0:A9:F6\",\"ip\":\"192.168.1.213\",\"createTime\":1679612040000},{\"guestNetworkId\":494,\"guestId\":212,\"networkId\":17,\"deviceId\":1,\"driveType\":\"virtio\",\"mac\":\"00:1D:B5:31:9B:76\",\"ip\":\"192.168.1.214\",\"createTime\":1681430689000},{\"guestNetworkId\":495,\"guestId\":223,\"networkId\":17,\"deviceId\":1,\"driveType\":\"virtio\",\"mac\":\"00:12:4B:9E:B5:7D\",\"ip\":\"192.168.1.215\",\"createTime\":1679612040000},{\"guestNetworkId\":496,\"guestId\":186,\"networkId\":17,\"deviceId\":1,\"driveType\":\"virtio\",\"mac\":\"00:04:EA:62:A8:A9\",\"ip\":\"192.168.1.216\",\"createTime\":1679612040000},{\"guestNetworkId\":497,\"guestId\":213,\"networkId\":17,\"deviceId\":1,\"driveType\":\"virtio\",\"mac\":\"00:19:07:3F:98:C5\",\"ip\":\"192.168.1.217\",\"createTime\":1679612040000},{\"guestNetworkId\":498,\"guestId\":116,\"networkId\":17,\"deviceId\":0,\"driveType\":\"virtio\",\"mac\":\"00:50:F0:93:2E:89\",\"ip\":\"192.168.1.218\",\"createTime\":1679612040000},{\"guestNetworkId\":499,\"guestId\":169,\"networkId\":17,\"deviceId\":0,\"driveType\":\"virtio\",\"mac\":\"00:22:B4:1D:23:39\",\"ip\":\"192.168.1.219\",\"createTime\":1679612040000},{\"guestNetworkId\":500,\"guestId\":170,\"networkId\":17,\"deviceId\":0,\"driveType\":\"virtio\",\"mac\":\"00:16:B8:7C:CC:CC\",\"ip\":\"192.168.1.220\",\"createTime\":1679612040000},{\"guestNetworkId\":501,\"guestId\":171,\"networkId\":17,\"deviceId\":0,\"driveType\":\"virtio\",\"mac\":\"00:60:38:1B:AF:2C\",\"ip\":\"192.168.1.221\",\"createTime\":1679612040000},{\"guestNetworkId\":502,\"guestId\":214,\"networkId\":17,\"deviceId\":1,\"driveType\":\"virtio\",\"mac\":\"00:23:7D:60:7E:58\",\"ip\":\"192.168.1.222\",\"createTime\":1679612040000},{\"guestNetworkId\":503,\"guestId\":224,\"networkId\":17,\"deviceId\":0,\"driveType\":\"virtio\",\"mac\":\"00:19:07:68:BD:6B\",\"ip\":\"192.168.1.223\",\"createTime\":1679612040000},{\"guestNetworkId\":504,\"guestId\":225,\"networkId\":17,\"deviceId\":0,\"driveType\":\"virtio\",\"mac\":\"00:14:04:31:F9:64\",\"ip\":\"192.168.1.224\",\"createTime\":1679612040000},{\"guestNetworkId\":505,\"guestId\":248,\"networkId\":17,\"deviceId\":1,\"driveType\":\"virtio\",\"mac\":\"00:24:E9:AD:D6:3D\",\"ip\":\"192.168.1.225\",\"createTime\":1679612040000},{\"guestNetworkId\":506,\"guestId\":249,\"networkId\":17,\"deviceId\":1,\"driveType\":\"virtio\",\"mac\":\"00:13:0A:D4:8C:E8\",\"ip\":\"192.168.1.226\",\"createTime\":1679612040000},{\"guestNetworkId\":507,\"guestId\":253,\"networkId\":17,\"deviceId\":0,\"driveType\":\"virtio\",\"mac\":\"00:1C:F9:62:FE:66\",\"ip\":\"192.168.1.227\",\"createTime\":1679612040000},{\"guestNetworkId\":508,\"guestId\":0,\"networkId\":17,\"deviceId\":0,\"driveType\":\"\",\"mac\":\"00:10:0B:5A:EE:39\",\"ip\":\"192.168.1.228\",\"createTime\":1679612040000},{\"guestNetworkId\":509,\"guestId\":0,\"networkId\":17,\"deviceId\":0,\"driveType\":\"\",\"mac\":\"00:1E:46:EE:45:31\",\"ip\":\"192.168.1.229\",\"createTime\":1679612040000},{\"guestNetworkId\":510,\"guestId\":0,\"networkId\":17,\"deviceId\":0,\"driveType\":\"\",\"mac\":\"00:1D:73:9D:6E:22\",\"ip\":\"192.168.1.230\",\"createTime\":1679612040000}]",new TypeToken<List<GuestNetworkEntity>>(){}.getType());

        String xml =  new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/route/dnsmasq.tpl")), StandardCharsets.UTF_8);
        Jinjava jinjava = new Jinjava();
        Map<String, Object> map = new HashMap<>();
        map.put("ip","192.168.1.210");
        map.put("startIp",network.getStartIp());
        map.put("endIp",network.getEndIp());
        map.put("gateway",network.getGateway());
        map.put("mask",network.getMask());
        map.put("domain","cj.local");
        map.put("dnsList", Arrays.asList(network.getDns().split(",")));
        List<Map<String,Object>> dhcpList=allGuestNetwork.stream().map(guestNetwork -> {
            Map<String,Object> dhcp=new HashMap<>(2);
            dhcp.put("mac",guestNetwork.getMac());
            dhcp.put("ip",guestNetwork.getIp());
            return dhcp;
        }).collect(Collectors.toList());
        map.put("dhcpList",dhcpList);

        xml = jinjava.render(xml, map);
        System.out.println(xml);
    }

    public static void testNetwork() {
        String body = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/network/network.tpl")), StandardCharsets.UTF_8);
        Jinjava jinjava = new Jinjava();
        Map<String, Object> map = new HashMap<>();
        map.put("index", 1);
        map.put("ip", "192.168.1.210");
        map.put("gateway", "192.168.1.1");
        map.put("netmask", "255.255.255.0");

        List<String> dnsList=Arrays.stream("192.168.1.2,192.168.1.1,8.8.8.8".split(",")).filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        map.put("dnsList", dnsList);
        body = jinjava.render(body, map).replaceAll("(?m)^[ \t]*\r?\n", "");
        System.out.println(body);
    }
}
