package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.HostOperate;
import cn.chenjun.cloud.agent.operate.NetworkOperate;
import cn.chenjun.cloud.agent.operate.StorageOperate;
import cn.chenjun.cloud.agent.util.ClientService;
import cn.chenjun.cloud.common.bean.*;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.libvirt.Connect;
import org.libvirt.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.util.List;
import java.util.Objects;

/**
 * @author chenjun
 */
@Component
public class HostOperateImpl implements HostOperate {
    @Autowired
    private NetworkOperate networkOperate;
    @Autowired
    private StorageOperate storageOperate;
    @Autowired
    private ClientService clientService;

    private static String getNodeText(Document doc,  String path, String defaultValue) {
            Node node = doc.selectSingleNode(path);
            if (node != null) {
                return node.getText();
            }
            return defaultValue;
    }
    private static String getArch(String xml) throws SAXException, DocumentException {

        try (StringReader sr = new StringReader(xml)) {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(sr);
            return getNodeText(doc, "/capabilities/host/cpu/arch", "x86_64");
        }
    }

    private static String getEmulator(String xml, String hostArch) throws SAXException, DocumentException {
        String emulator = null;
        try (StringReader sr = new StringReader(xml)) {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(sr);
            String path = "/capabilities/guest";
            List<Element> nodes = doc.selectNodes(path);
            for (Element node : nodes) {
                Object osType = node.selectObject("os_type");
                Object arch = node.selectObject("arch");
                boolean isHvm = false;
                boolean isArch = false;
                if (osType instanceof Element) {
                    isHvm = "hvm".equals(((Element) osType).getData());
                }
                if (arch instanceof Element) {
                    String archValue = ((Element) node.selectObject("arch")).attribute("name").getText();
                    isArch = Objects.equals(hostArch, archValue);
                }
                if (isHvm && isArch) {

                    Object emulatorNode = node.selectObject("arch/emulator");
                    if (emulatorNode instanceof Element) {
                        emulator = ((Element) emulatorNode).getTextTrim();
                        //break;
                    }
                    List<Element> domainNodes = node.selectNodes("arch/domain");
                    for (Element domainNode : domainNodes) {
                        Attribute attribute = domainNode.attribute("type");
                        if (attribute != null && Objects.equals(attribute.getValue(), "kvm")) {
                            emulatorNode = domainNode.selectObject("emulator");
                            if (emulatorNode instanceof Element) {
                                emulator = ((Element) emulatorNode).getTextTrim();
                            }
                        }
                    }
                }

            }
        }
        return emulator;
    }

    @Override
    public HostInfo getHostInfo(Connect connect) throws Exception {
        OsInfo osInfo = SystemUtil.getOsInfo();
        String xml = connect.getCapabilities();
        String arch = getArch(xml);
        String emulator = getEmulator(xml, arch);
        NodeInfo nodeInfo = connect.nodeInfo();
        return HostInfo.builder().hostName(connect.getHostName())
                .version(connect.getVersion())
                .uri(connect.getURI())
                .memory(nodeInfo.memory)
                .cpu(nodeInfo.cpus)
                .hypervisor(connect.getType())
                .arch(arch)
                .name(osInfo.getName())
                .cores(nodeInfo.cores)
                .threads(nodeInfo.threads)
                .sockets(nodeInfo.sockets)
                .emulator(emulator)
                .build();
    }

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
        return this.getHostInfo(connect);
    }
}
