package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.NetworkOperate;
import cn.chenjun.cloud.agent.operate.annotation.DispatchBind;
import cn.chenjun.cloud.common.bean.BasicBridgeNetwork;
import cn.chenjun.cloud.common.bean.VlanNetwork;
import cn.chenjun.cloud.common.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.libvirt.Connect;
import org.libvirt.Network;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class NetworkOperateImpl implements NetworkOperate {
    private static String getNodeAttr(String xml, String path, String attrName) throws SAXException, DocumentException {
        try (StringReader sr = new StringReader(xml)) {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(sr);
            Element node = (Element) doc.selectSingleNode(path);
            return node == null ? null : node.attributeValue(attrName);
        }
    }

    @DispatchBind(command = Constant.Command.NETWORK_CREATE_BASIC)
    @Override
    public Void createBasic(Connect connect, BasicBridgeNetwork request) throws Exception {
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
    @Override
    public Void createVlan(Connect connect, VlanNetwork vlan) throws Exception {
        log.info("创建Vlan网络:{}", vlan);
        createNetworkPool(connect, vlan.getPoolId(), vlan.getBasic().getXml());
        createNetworkPool(connect, vlan.getPoolId(), vlan.getXml());
        return null;
    }

    @DispatchBind(command = Constant.Command.NETWORK_DESTROY_BASIC)
    @Override
    public Void destroyBasic(Connect connect, BasicBridgeNetwork bridge) throws Exception {
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
    @Override
    public Void destroyVlan(Connect connect, VlanNetwork vlan) throws Exception {
        log.info("销毁Vlan网络:{}", vlan);
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
