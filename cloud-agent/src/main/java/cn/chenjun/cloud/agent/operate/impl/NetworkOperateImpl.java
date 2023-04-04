package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.config.ApplicationConfig;
import cn.chenjun.cloud.agent.operate.NetworkOperate;
import cn.chenjun.cloud.agent.util.NetworkType;
import cn.chenjun.cloud.common.bean.BasicBridgeNetwork;
import cn.chenjun.cloud.common.bean.VlanNetwork;
import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.Network;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class NetworkOperateImpl implements NetworkOperate {


    @Autowired
    private ApplicationConfig applicationConfig;
    @Override
    public void createBasic(Connect connect, BasicBridgeNetwork request) throws Exception {
        log.info("创建基础网络:{} type={}", request, applicationConfig.getNetworkType());
        List<String> networkNames = Arrays.asList(connect.listNetworks());
        if (!networkNames.contains(request.getBridge())) {
            if (NetworkType.OPEN_SWITCH.equalsIgnoreCase(applicationConfig.getNetworkType())) {
                String xml = ResourceUtil.readUtf8Str("xml/network/OpenSwitch.xml");
                connect.networkCreateXML(String.format(xml, request.getBridge(), request.getBridge()));
            } else if (NetworkType.BRIDGE.equalsIgnoreCase(applicationConfig.getNetworkType())) {
                String xml = ResourceUtil.readUtf8Str("xml/network/Bridge.xml");
                connect.networkCreateXML(String.format(xml, request.getBridge(), request.getBridge()));
            }
        }

    }

    @Override
    public void createVlan(Connect connect, VlanNetwork vlan) throws Exception {
        log.info("创建Vlan网络:{}",vlan);
    }

    @Override
    public void destroyBasic(Connect connect, BasicBridgeNetwork bridge) throws Exception {
        log.info("销毁基础网络:{}", bridge);
        List<String> networkNames = Arrays.asList(connect.listNetworks());
        if (!networkNames.contains(bridge.getBridge())) {
            try {
                Network network = connect.networkLookupByName(bridge.getBridge());
                network.destroy();
            } catch (Exception err) {

            }
        }


    }

    @Override
    public void destroyVlan(Connect connect, VlanNetwork vlan) throws Exception {
        log.info("销毁Vlan网络:{}",vlan);
    }



}
