package cn.ruiyun.cloud.agent.operate.impl;

import cn.ruiyun.cloud.common.bean.BasicBridgeNetworkRequest;
import cn.ruiyun.cloud.common.bean.VLanNetworkRequest;
import cn.ruiyun.cloud.common.bean.VxLanNetworkRequest;
import cn.ruiyun.cloud.common.core.annotation.DispatchBind;
import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.Network;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class NetworkOperate {

    @DispatchBind(command = Constant.Command.NETWORK_CREATE_BASIC)
    public Void createBasic(Connect connect, BasicBridgeNetworkRequest request) throws Exception {
        createNetworkPool(connect, request.getPoolId(), request.getXml());
        return null;
    }

    private void createNetworkPool(Connect connect, String name, String xml) throws Exception {

        log.info("创建网络:{},xml={}", name, xml);
        Network network;
        try {
            network = connect.networkLookupByName(name);
        } catch (Exception err) {
            network = connect.networkDefineXML(xml);
            network.setAutostart(true);
        }
        if (network.isActive() == 0) {
            network.create();
        }
    }

    @DispatchBind(command = Constant.Command.NETWORK_CREATE_VLAN)
    public Void createVlan(Connect connect, VLanNetworkRequest vlan) throws Exception {
        log.info("创建Van网络:{}", vlan);
        createNetworkPool(connect, vlan.getPoolId(), vlan.getBasic().getXml());
        createNetworkPool(connect, vlan.getPoolId(), vlan.getXml());
        return null;
    }

    @DispatchBind(command = Constant.Command.NETWORK_DESTROY_BASIC)
    public Void destroyBasic(Connect connect, BasicBridgeNetworkRequest bridge) throws Exception {
        log.info("销毁基础网络:{}", bridge);
        List<String> networkNames = Arrays.asList(connect.listNetworks());
        if (networkNames.contains(bridge.getPoolId())) {
            try {
                Network network = connect.networkLookupByName(bridge.getPoolId());
                network.destroy();
                network.undefine();
            } catch (Exception ignored) {

            }
        }
        return null;

    }

    @DispatchBind(command = Constant.Command.NETWORK_DESTROY_VLAN)
    public Void destroyVlan(Connect connect, VLanNetworkRequest vlan) throws Exception {
        log.info("销毁VLan网络:{}", vlan);
        List<String> networkNames = Arrays.asList(connect.listNetworks());
        if (networkNames.contains(vlan.getPoolId())) {
            try {
                Network network = connect.networkLookupByName(vlan.getPoolId());
                network.destroy();
                network.undefine();
            } catch (Exception ignored) {

            }
        }
        return null;
    }

}
