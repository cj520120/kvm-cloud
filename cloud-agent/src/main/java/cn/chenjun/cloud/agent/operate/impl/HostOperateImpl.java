package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.HostOperate;
import cn.chenjun.cloud.agent.operate.NetworkOperate;
import cn.chenjun.cloud.agent.operate.StorageOperate;
import cn.chenjun.cloud.agent.operate.annotation.DispatchBind;
import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.util.Constant;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.libvirt.Connect;
import org.libvirt.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;

/**
 * @author chenjun
 */
@Component
public class HostOperateImpl implements HostOperate {
    @Autowired
    private NetworkOperate networkOperate;
    @Autowired
    private StorageOperate storageOperate;



    @SuppressWarnings("unchecked")
    @SneakyThrows
    private HostInfo getHostInfo(Connect connect) {
        NodeInfo nodeInfo = connect.nodeInfo();
        OsInfo osInfo = SystemUtil.getOsInfo();
        HostInfo hostInfo = HostInfo.builder().hostName(connect.getHostName())
                .name(osInfo.getName())
                .osVersion(osInfo.getVersion())
                .arch(osInfo.getArch())
                .version(connect.getVersion())
                .uri(connect.getURI())
                .memory(nodeInfo.memory)
                .cpu(nodeInfo.cpus)
                .hypervisor(connect.getType())
                .cores(nodeInfo.cores)
                .threads(nodeInfo.threads)
                .sockets(nodeInfo.sockets)
                .build();
        try (StringReader sr = new StringReader(connect.getCapabilities())) {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(sr);
            Node archNode = doc.selectSingleNode("/capabilities/host/cpu/arch");
            if (archNode != null) {
                hostInfo.setArch(archNode.getText());
            }
            Node vendorNode = doc.selectSingleNode("/capabilities/host/cpu/vendor");
            if (vendorNode != null) {
                hostInfo.setVendor(vendorNode.getText());
            }
            Node topologyNode = doc.selectSingleNode("/capabilities/host/cpu/topology");
            if (topologyNode instanceof Element) {
                hostInfo.setSockets(NumberUtil.parseInt(((Element) topologyNode).attributeValue("sockets")));
                hostInfo.setCores(NumberUtil.parseInt(((Element) topologyNode).attributeValue("cores")));
                hostInfo.setThreads(NumberUtil.parseInt(((Element) topologyNode).attributeValue("threads")));
            }
            List<Element> guestNodes = Collections.unmodifiableList(doc.selectNodes("/capabilities/guest"));
            for (Element guestNode : guestNodes) {
                String osType = guestNode.selectSingleNode("os_type").getText();
                String arch = ((Element) guestNode.selectSingleNode("arch")).attributeValue("name");
                if (StringUtils.equalsIgnoreCase(osType, "hvm") && StringUtils.equalsIgnoreCase(arch, hostInfo.getArch())) {
                    Node emulatorNode = guestNode.selectSingleNode("arch/emulator");
                    if (emulatorNode != null) {
                        hostInfo.setEmulator(emulatorNode.getText());
                    }
                    List<Element> guestDomainNodes = guestNode.selectNodes("arch/domain");
                    for (Element guestDomainNode : guestDomainNodes) {
                        emulatorNode = guestDomainNode.selectSingleNode("emulator");
                        if (emulatorNode != null) {
                            hostInfo.setEmulator(emulatorNode.getText());
                            break;
                        }
                    }
                }
                if (!StringUtils.isEmpty(hostInfo.getEmulator())) {
                    break;
                }
            }
        }
        return hostInfo;
    }

    @DispatchBind(command = Constant.Command.HOST_INFO)
    @Override
    public HostInfo getHostInfo(Connect connect, NoneRequest request) {

        return getHostInfo(connect);
    }

    @DispatchBind(command = Constant.Command.HOST_INIT)
    @Override
    public HostInfo initHost(Connect connect, InitHostRequest request) throws Exception {

        List<StorageCreateRequest> storageList = request.getStorageList();
        if (storageList != null) {
            for (StorageCreateRequest storage : storageList) {
                this.storageOperate.create(connect, storage);
            }
        }
        List<BasicBridgeNetwork> basicBridgeNetworkList = request.getBasicBridgeNetworkList();
        if (basicBridgeNetworkList != null) {
            for (BasicBridgeNetwork basicBridgeNetwork : basicBridgeNetworkList) {
                this.networkOperate.createBasic(connect, basicBridgeNetwork);
            }
        }
        List<VlanNetwork> vlanNetworkList = request.getVlanNetworkList();
        if (vlanNetworkList != null) {
            for (VlanNetwork vlanNetwork : vlanNetworkList) {
                this.networkOperate.createVlan(connect, vlanNetwork);
            }
        }
        return this.getHostInfo(connect, null);
    }
}
