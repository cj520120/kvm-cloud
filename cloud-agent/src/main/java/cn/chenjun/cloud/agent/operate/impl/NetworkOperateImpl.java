package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.NetworkOperate;
import cn.chenjun.cloud.agent.operate.annotation.DispatchBind;
import cn.chenjun.cloud.common.bean.BasicBridgeNetwork;
import cn.chenjun.cloud.common.bean.VlanNetwork;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.io.resource.ResourceUtil;
import com.hubspot.jinjava.Jinjava;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.Network;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class NetworkOperateImpl implements NetworkOperate {


    @DispatchBind(command = Constant.Command.NETWORK_CREATE_BASIC)
    @Override
    public Void createBasic(Connect connect, BasicBridgeNetwork request) throws Exception {
        log.info("创建基础网络:{} type={}", request, request.getBridgeType().bridgeName());
        List<String> networkNames = Arrays.asList(connect.listNetworks());
        if (!networkNames.contains(request.getBridge())) {
            Map<String, Object> map = new HashMap<>(3);
            map.put("name", request.getBridge());
            map.put("bridge", request.getBridge());
            map.put("type", request.getBridgeType().bridgeName());
            String xml = ResourceUtil.readUtf8Str("tpl/network.xml");
            Jinjava jinjava = new Jinjava();
            connect.networkCreateXML(jinjava.render(xml, map));
        }
        return null;

    }

    @DispatchBind(command = Constant.Command.NETWORK_CREATE_VLAN)
    @Override
    public Void createVlan(Connect connect, VlanNetwork vlan) throws Exception {
        log.info("创建Vlan网络:{}", vlan);
        String vlanBridgeName = vlan.getBasic().getBridge() + "-vlan-" + vlan.getVlanId();
        List<String> networkNames = Arrays.asList(connect.listNetworks());
        if (!networkNames.contains(vlanBridgeName)) {
            if (Objects.requireNonNull(vlan.getBasic().getBridgeType()) == Constant.NetworkBridgeType.OPEN_SWITCH) {
                Map<String, Object> map = new HashMap<>(3);
                map.put("name", vlanBridgeName);
                map.put("bridge", vlan.getBasic().getBridge());
                map.put("type", vlan.getBasic().getBridgeType().bridgeName());
                map.put("vlanId", vlan.getVlanId());
                String xml = ResourceUtil.readUtf8Str("tpl/vlan.xml");
                Jinjava jinjava = new Jinjava();
                connect.networkCreateXML(jinjava.render(xml, map));
            } else {
                throw new CodeException(ErrorCode.SERVER_ERROR, "vlan只支持OpenSwitch的基础网络");
            }

        }
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
    public Void destroyVlan(Connect connect, VlanNetwork vlan) throws Exception {
        log.info("销毁Vlan网络:{}", vlan);
        String vlanBridgeName = vlan.getBasic().getBridge() + "-vlan-" + vlan.getVlanId();
        List<String> networkNames = Arrays.asList(connect.listNetworks());
        if (!networkNames.contains(vlanBridgeName)) {
            try {
                Network network = connect.networkLookupByName(vlanBridgeName);
                network.destroy();
            } catch (Exception ignored) {

            }
        }
        return null;
    }

}
