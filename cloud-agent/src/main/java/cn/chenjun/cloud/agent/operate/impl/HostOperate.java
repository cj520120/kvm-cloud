package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.annotation.DispatchBind;
import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.util.Constant;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.SystemPropsUtil;
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
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenjun
 */
@Component
public class HostOperate {
    @Autowired
    private NetworkOperate networkOperate;
    @Autowired
    private StorageOperate storageOperate;


    @SneakyThrows
    private HostInfo getHostInfo(Connect connect) {
        NodeInfo nodeInfo = connect.nodeInfo();
        List<HostInfo.Topology> cpuTopology = new ArrayList<>();
        HostInfo.Cpu cpu = HostInfo.Cpu.builder().number(nodeInfo.cpus).topology(cpuTopology).build();
        HostInfo hostInfo = HostInfo.builder().hostName(connect.getHostName())
                .name(SystemPropsUtil.get("os.name", ""))
                .osVersion(SystemPropsUtil.get("os.version", ""))
                .version(connect.getVersion())
                .uri(connect.getURI())
                .memory(nodeInfo.memory)
                .hypervisor(connect.getType())
                .cpu(cpu)
                .build();
        try (StringReader sr = new StringReader(connect.getCapabilities())) {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(sr);
            Node archNode = doc.selectSingleNode("/capabilities/host/cpu/arch");
            if (archNode != null) {
                cpu.setArch(archNode.getText());
            }
            Node counterNode = doc.selectSingleNode("/capabilities/host/cpu/counter");
            if (counterNode != null) {
                cpu.setFrequency(NumberUtil.parseLong(((Element) counterNode).attributeValue("frequency")));
            }
            Node cpuModelNodel = doc.selectSingleNode("/capabilities/host/cpu/model");
            if (cpuModelNodel != null) {
                cpu.setModel(cpuModelNodel.getText());
            }
            Node vendorNode = doc.selectSingleNode("/capabilities/host/cpu/vendor");
            if (vendorNode != null) {
                cpu.setVendor(vendorNode.getText());
            }

            List<Node> cpuCellNodes = doc.selectNodes("/capabilities/host/topology/cells/cell");
            cpu.setSockets(cpuCellNodes.size());
            cpu.setCores(nodeInfo.cores);
            cpu.setThreads(nodeInfo.threads);
            for (Node node : cpuCellNodes) {
                HostInfo.Topology topology = new HostInfo.Topology();
                cpuTopology.add(topology);
                topology.setId(NumberUtil.parseInt(((Element) node).attributeValue("id")));
                Node cpuNode = node.selectSingleNode("cpus");
                topology.setNumber(NumberUtil.parseInt(((Element) cpuNode).attributeValue("num")));
                List<HostInfo.Topology.Cell> cells = new ArrayList<>();
                topology.setCells(cells);
                List<Node> cellNodes = cpuNode.selectNodes("cpu");
                for (Node cellNode : cellNodes) {
                    Element element = (Element) cellNode;
                    int id = NumberUtil.parseInt(element.attributeValue("id"));
                    int socketId = NumberUtil.parseInt(element.attributeValue("socket_id"));
                    int coreId = NumberUtil.parseInt(element.attributeValue("core_id"));
                    HostInfo.Topology.Cell cell = HostInfo.Topology.Cell.builder().id(id).socketId(socketId).coreId(coreId).build();
                    cells.add(cell);
                }
            }
            List<Node> guestNodes = doc.selectNodes("/capabilities/guest");
            for (Node node : guestNodes) {
                String osType = node.selectSingleNode("os_type").getText();
                String arch = ((Element) node.selectSingleNode("arch")).attributeValue("name");
                if (StringUtils.equalsIgnoreCase(osType, "hvm") && StringUtils.equalsIgnoreCase(arch, cpu.getArch())) {
                    Node emulatorNode = node.selectSingleNode("arch/emulator");
                    if (emulatorNode != null) {
                        hostInfo.setEmulator(emulatorNode.getText());
                    }
                    List<Node> guestDomainNodes = node.selectNodes("arch/domain[@type='kvm']");
                    for (Node guestDomainNode : guestDomainNodes) {
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
    public HostInfo getHostInfo(Connect connect, NoneRequest request) {

        return getHostInfo(connect);
    }

    @DispatchBind(command = Constant.Command.HOST_INIT, async = true)
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
