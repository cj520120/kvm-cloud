package cn.roamblue.cloud.agent.operate.impl;

import cn.hutool.core.util.RuntimeUtil;
import cn.roamblue.cloud.agent.operate.NetworkOperate;
import cn.roamblue.cloud.common.agent.NetworkRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import org.libvirt.Connect;

import java.net.NetworkInterface;

/**
 * @author chenjun
 */
public class NetworkOperateImpl implements NetworkOperate {


    @Override
    public void createBasic(Connect connect, NetworkRequest request) throws Exception {
        addBridge(request.getBasicBridge().getNic(), request.getBasicBridge().getBridge(), request.getBasicBridge().getIp(), request.getBasicBridge().getNetmask());
        shell("route add default gw " + request.getBasicBridge().getGeteway() + " " + request.getBasicBridge().getBridge());
    }

    @Override
    public void createVlan(Connect connect, NetworkRequest request) throws Exception {
        shell("modprobe 8021q");
        NetworkRequest.Vlan vlan = request.getVlan();
        NetworkRequest.BasicBridge bridge = request.getBasicBridge();
        String nic = request.getBasicBridge().getBridge();
        String vlanNic = nic + "." + vlan.getVlanId();
        shell("vconfig add " + bridge.getBridge() + " " + vlan.getVlanId());
        shell("vconfig set_flag " + vlanNic + " 1 1");
        shell("ip link set " + vlanNic + " up");
        addBridge(vlanNic, vlan.getBridge(), vlan.getIp(), vlan.getNetmask());
    }

    @Override
    public void destroyBasic(Connect connect, NetworkRequest request) throws Exception {
        NetworkRequest.BasicBridge bridge = request.getBasicBridge();
        shell("ip link set " + bridge.getBridge() + " down");
        shell("brctl delbr " + bridge.getBridge());
        shell("ifconfig " + bridge.getNic() + " " + bridge.getIp() + " netmask " + bridge.getNetmask() + " up");
        shell("route add default gw " + bridge.getGeteway() + " " + bridge.getNic());
    }

    @Override
    public void destroyVlan(Connect connect, NetworkRequest request) throws Exception {
        NetworkRequest.Vlan vlan = request.getVlan();
        NetworkRequest.BasicBridge bridge = request.getBasicBridge();
        String vlanNic = bridge.getBridge() + "." + vlan.getVlanId();
        shell("ip link set " + vlan.getBridge() + " down");
        shell("brctl delbr " + vlan.getBridge());
        shell("ip link set " + vlanNic + " down");
        shell("vconfig rem " + vlanNic);
    }

    private void addBridge(String nic, String bridge, String ip, String netmask) throws Exception {
        if (NetworkInterface.getByName(bridge) == null) {
            shell("brctl addbr " + bridge);
            shell("brctl stp " + bridge + " on");
        }
        shell("brctl addif " + bridge + " " + nic);
        shell("ip link set " + bridge + " up");
        shell("ifconfig " + nic + " 0.0.0.0");
        shell("ifconfig " + bridge + " " + ip + " netmask " + netmask + " up");
    }

    private void shell(String command) {
        Process process = RuntimeUtil.exec(command);
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
}
