package cn.roamblue.test;

import cn.hutool.core.util.RuntimeUtil;
import cn.roamblue.cloud.agent.operate.NetworkOperate;
import cn.roamblue.cloud.agent.operate.impl.NetworkOperateImpl;
import cn.roamblue.cloud.common.agent.NetworkRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Command;
import cn.roamblue.cloud.common.util.ErrorCode;
import org.libvirt.Connect;
import org.springframework.util.StringUtils;

import java.net.NetworkInterface;

public class NetworkTest {
    public static void main(String[] args) throws Exception{

        Connect connect = new Connect("qemu:///system");
        NetworkOperate operate=new NetworkOperateImpl();
        NetworkRequest request=  NetworkRequest.builder()
                .command(Command.Network.CREATE_BASIC)
                .basicBridge(NetworkRequest.BasicBridge.builder().bridge("br0").ip("192.168.1.69").geteway("192.168.1.1").nic("ens20").netmask("255.255.255.0").build())
                .vlan(NetworkRequest.Vlan.builder().vlanId(100).bridge("vlan.100").ip("192.168.3.2").netmask("255.255.255.0").geteway("192.168.3.1").build())
                .build();
        operate.createBasic(connect,request);
        operate.createVlan(connect,request);
        operate.destroyVlan(connect,request);
        operate.destroyBasic(connect,request);
    }
}
