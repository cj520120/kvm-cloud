package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.NetworkOperate;
import cn.chenjun.cloud.agent.operate.annotation.DispatchBind;
import cn.chenjun.cloud.agent.util.TemplateUtil;
import cn.chenjun.cloud.common.bean.BasicBridgeNetwork;
import cn.chenjun.cloud.common.bean.VlanNetwork;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.io.resource.ResourceUtil;
import com.hubspot.jinjava.Jinjava;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.libvirt.Connect;
import org.libvirt.Network;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.util.*;

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
        log.info("创建基础网络:{} type={}", request, request.getBridgeType().bridgeName());
        List<String> networkNames = Arrays.asList(connect.listNetworks());
        boolean canCreated;
        if (!networkNames.contains(request.getPoolId())) {
            canCreated = true;

        } else {
            Network network = connect.networkLookupByName(request.getPoolId());
            String xml = network.getXMLDesc(0);
            log.info("网络配置已存在 XML:{}={}", request.getPoolId(), xml);
            canCreated = ObjectUtils.notEqual(request.getBridge(), getNodeAttr(xml, "/network/bridge", "name"))
                    || ObjectUtils.notEqual("bridge", getNodeAttr(xml, "/network/forward", "mode"));
            if (!canCreated && Objects.requireNonNull(request.getBridgeType()) == Constant.NetworkBridgeType.OPEN_SWITCH) {
                canCreated = ObjectUtils.notEqual("openvswitch", getNodeAttr(xml, "/network/virtualport", "type"));
            }
            if (canCreated) {
                log.info("网络配置不匹配，删除旧的网络配置:{}", request.getPoolId());
                network.destroy();
            }
        }
        if (canCreated) {
            Map<String, Object> map = new HashMap<>(3);
            map.put("name", request.getPoolId());
            map.put("uuid", request.getPoolId());
            map.put("description", "Basic Network " + request.getBridge());
            map.put("bridge", request.getBridge());
            map.put("type", request.getBridgeType().bridgeName());
            map.put("vlanId", 0);
            String xml = ResourceUtil.readUtf8Str("tpl/network.xml");
            Jinjava jinjava = TemplateUtil.create();
            xml = jinjava.render(xml, map);
            log.info("create basic network xml={}", xml);
            connect.networkCreateXML(xml);
        }
        return null;

    }

    @DispatchBind(command = Constant.Command.NETWORK_CREATE_VLAN)
    @Override
    public Void createVlan(Connect connect, VlanNetwork vlan) throws Exception {
        log.info("创建Vlan网络:{}", vlan);
        List<String> networkNames = Arrays.asList(connect.listNetworks());
        boolean canCreated;
        if (!networkNames.contains(vlan.getPoolId())) {
            canCreated = true;

        } else {

            Network network = connect.networkLookupByName(vlan.getPoolId());
            String xml = network.getXMLDesc(0);
            log.info("网络配置已存在 XML:{}={}", vlan.getPoolId(), xml);
            canCreated = ObjectUtils.notEqual(vlan.getBridge(), getNodeAttr(xml, "/network/bridge", "name"))
                    || ObjectUtils.notEqual("bridge", getNodeAttr(xml, "/network/forward", "mode"))
                    || ObjectUtils.notEqual("openvswitch", getNodeAttr(xml, "/network/virtualport", "type"))
                    || ObjectUtils.notEqual(String.valueOf(vlan.getVlanId()), getNodeAttr(xml, "/network/portgroup/vlan/tag", "id"))
                    || ObjectUtils.notEqual(String.valueOf(vlan.getVlanId()), getNodeAttr(xml, "/network/vlan/tag", "id"));

            if (canCreated) {
                log.info("网络配置不匹配，删除旧的网络配置:{}", vlan.getPoolId());
                network.destroy();
            }
        }
        if (canCreated) {
            if (Objects.requireNonNull(vlan.getBasic().getBridgeType()) == Constant.NetworkBridgeType.OPEN_SWITCH) {
                Map<String, Object> map = new HashMap<>(3);
                map.put("name", vlan.getPoolId());
                map.put("uuid", vlan.getPoolId());
                map.put("description", "Vlan Network " + vlan.getBridge() + "-" + vlan.getVlanId());
                map.put("bridge", vlan.getBasic().getBridge());
                map.put("type", vlan.getBasic().getBridgeType().bridgeName());
                map.put("vlanId", vlan.getVlanId());
                String xml = ResourceUtil.readUtf8Str("tpl/network.xml");
                Jinjava jinjava = TemplateUtil.create();
                xml = jinjava.render(xml, map);
                log.info("create vlan network xml={}", xml);
                connect.networkCreateXML(xml);
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
        if (networkNames.contains(bridge.getPoolId())) {
            try {
                Network network = connect.networkLookupByName(bridge.getPoolId());
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
        List<String> networkNames = Arrays.asList(connect.listNetworks());
        if (networkNames.contains(vlan.getPoolId())) {
            try {
                Network network = connect.networkLookupByName(vlan.getPoolId());
                network.destroy();
            } catch (Exception ignored) {

            }
        }
        return null;
    }

}
