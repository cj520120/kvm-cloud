package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.config.ApplicationConfig;
import cn.chenjun.cloud.agent.operate.NetworkOperate;
import cn.chenjun.cloud.agent.operate.annotation.DispatchBind;
import cn.chenjun.cloud.common.bean.BasicBridgeNetwork;
import cn.chenjun.cloud.common.bean.VlanNetwork;
import cn.chenjun.cloud.common.util.Constant;
import cn.hutool.core.io.resource.ResourceUtil;
import com.hubspot.jinjava.Jinjava;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.Network;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class NetworkOperateImpl implements NetworkOperate {


    @Autowired
    private ApplicationConfig applicationConfig;

    @DispatchBind(command = Constant.Command.NETWORK_CREATE_BASIC)
    @Override
    public Void createBasic(Connect connect, BasicBridgeNetwork request) throws Exception {
        log.info("创建基础网络:{} type={}", request, applicationConfig.getNetworkType());
        List<String> networkNames = Arrays.asList(connect.listNetworks());
        if (!networkNames.contains(request.getBridge())) {
            Map<String, Object> map = new HashMap<>(3);
            map.put("name", request.getBridge());
            map.put("bridge", request.getBridge());
            map.put("type", applicationConfig.getNetworkType());
            String xml = ResourceUtil.readUtf8Str("tpl/network.xml");
            Jinjava jinjava = new Jinjava();
            connect.networkCreateXML(jinjava.render(xml, map));
        }
        return null;

    }

    @DispatchBind(command = Constant.Command.NETWORK_CREATE_VLAN)
    @Override
    public Void createVlan(Connect connect, VlanNetwork vlan) {
        log.info("创建Vlan网络:{}", vlan);
        return null;
    }

    @DispatchBind(command = Constant.Command.NETWORK_DESTROY_BASIC)
    @Override
    public Void destroyBasic(Connect connect, BasicBridgeNetwork bridge) throws Exception {
        log.info("销毁基础网络:{}", bridge);
        List<String> networkNames = Arrays.asList(connect.listNetworks());
        if (!networkNames.contains(bridge.getBridge())) {
            try {
                Network network = connect.networkLookupByName(bridge.getBridge());
                network.destroy();
            } catch (Exception ignored) {

            }
        }
        return null;


    }

    @DispatchBind(command = Constant.Command.NETWORK_DESTROY_VLAN)
    @Override
    public Void destroyVlan(Connect connect, VlanNetwork vlan) {
        log.info("销毁Vlan网络:{}", vlan);
        return null;
    }

}
