package cn.roamblue.cloud.agent.operate.impl;

import cn.hutool.core.util.RuntimeUtil;
import cn.roamblue.cloud.agent.operate.NetworkOperate;
import cn.roamblue.cloud.common.bean.BasicBridgeNetwork;
import cn.roamblue.cloud.common.bean.VlanNetwork;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import org.libvirt.Connect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.NetworkInterface;

/**
 * @author chenjun
 */
@Component
public class NetworkOperateImpl implements NetworkOperate {


    @Override
    public void createBasic(Connect connect, BasicBridgeNetwork request) throws Exception {
        addBridge(request.getNic(), request.getBridge(), request.getIp(), request.getNetmask(),request.getGeteway());

    }

    @Override
    public void createVlan(Connect connect, VlanNetwork vlan) throws Exception {
        shell("modprobe 8021q");
        BasicBridgeNetwork bridge = vlan.getBasic();
        String nic = bridge.getBridge();
        String vlanNic = nic + "." + vlan.getVlanId();
        if (NetworkInterface.getByName(vlanNic) == null) {
            shell("vconfig add " + bridge.getBridge() + " " + vlan.getVlanId());
            shell("vconfig set_flag " + vlanNic + " 1 1");
            shell("ip link set " + vlanNic + " up");
        }
        addBridge(vlanNic, vlan.getBridge(), vlan.getIp(), vlan.getNetmask(),vlan.getGeteway());
    }

    @Override
    public void destroyBasic(Connect connect, BasicBridgeNetwork bridge) throws Exception {
        if (NetworkInterface.getByName(bridge.getBridge()) != null) {
            shell("ip link set " + bridge.getBridge() + " down");
            shell("brctl delbr " + bridge.getBridge());
            shell("ifconfig " + bridge.getNic() + " " + bridge.getIp() + " netmask " + bridge.getNetmask() + " up");
            shell("route add default gw " + bridge.getGeteway() + " " + bridge.getNic());
        }
    }

    @Override
    public void destroyVlan(Connect connect, VlanNetwork vlan) throws Exception {
        BasicBridgeNetwork bridge = vlan.getBasic();
        String vlanNic = bridge.getBridge() + "." + vlan.getVlanId();
        if (NetworkInterface.getByName(bridge.getBridge()) != null) {
            shell("ip link set " + vlan.getBridge() + " down");
            if (NetworkInterface.getByName(vlan.getBridge()) != null) {
                shell("brctl delbr " + vlan.getBridge());
            }
            shell("ip link set " + vlanNic + " down");
            shell("vconfig rem " + vlanNic);
        }
    }

    private void addBridge(String nic, String bridge, String ip, String netmask,String gateway) throws Exception {
        if (NetworkInterface.getByName(bridge) == null) {
            shell("modprobe br_netfilter || true");
            shell("echo 1 > /proc/sys/net/bridge/bridge-nf-call-iptables");
            shell("echo 1 > /proc/sys/net/bridge/bridge-nf-filter-vlan-tagged");
            shell("echo 1 > /proc/sys/net/ipv4/conf/default/forwarding");
            shell("brctl addbr " + bridge);
            shell("brctl setfd " + bridge + " 0");
            shell("brctl stp " + bridge + " off");
            shell("ip link set " + bridge + " up");
            shell("brctl addif " + bridge + " " + nic);
            //切换mac地址
            shell(String.format("mac=`ip link show %s|grep ether|awk '{print $2}'`;ip link set %s address $mac", nic,bridge));

            String[] routes=shellForStr(String.format("ip route show dev %s | grep via | sed 's/onlink//g'", nic)).split("\n");
            for (String route : routes) {
                if(!StringUtils.isEmpty(route)) {
                    shell(String.format("ip route del %s", route));
                }
            }
            shell(String.format("ifconfig %s 0.0.0.0",nic));
            if(!StringUtils.isEmpty(ip)) {
                shell(String.format("ip addr add %s/%s dev %s", ip, netmask, bridge));
            }

            for (String route : routes) {
                if(!StringUtils.isEmpty(route)) {
                    shell(String.format("ip route add %s", route));
                }
            }
        };
    }

    private void shell(String command) {
        Process process = RuntimeUtil.exec(new String[]{"sh","-c",command});
        try {
            process.waitFor();
            int code = process.exitValue();
            if (code != 0) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "执行网络命令失败:" + command);
            }
        } catch (Exception err){
            throw new CodeException(ErrorCode.SERVER_ERROR, "执行网络命令失败:" + command,err);
        }finally {
            process.destroy();
        }
    }
    private String shellForStr(String command){
        return RuntimeUtil.execForStr(new String[]{"sh","-c",command});
    }

}
