package cn.roamblue.cloud.agent.operate.impl;

import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import cn.roamblue.cloud.agent.operate.HostOperate;
import cn.roamblue.cloud.agent.util.HostUtil;
import cn.roamblue.cloud.common.bean.HostInfo;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.libvirt.Connect;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.util.List;

/**
 * @author chenjun
 */
@Component
public class HostOperateImpl implements HostOperate {

    private static String getEmulator(String xml) throws SAXException, DocumentException {
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
                boolean isX86_64 = false;
                if (osType != null && osType instanceof Element) {
                    isHvm = "hvm".equals(((Element) osType).getData());
                }
                if (arch != null && arch instanceof Element) {
                    String archValue = ((Element) node.selectObject("arch")).attribute("name").getText();
                    isX86_64 = "x86_64".equals(archValue);
                }
                if (isHvm && isX86_64) {
                    Object emulatorNode = node.selectObject("arch/emulator");
                    if (emulatorNode != null && emulatorNode instanceof Element) {
                        emulator = ((Element) emulatorNode).getTextTrim();
                        break;
                    }
                }

            }
        }
        return emulator;
    }
    @Override
    public HostInfo getHostInfo(Connect connect) throws Exception {
        OsInfo osInfo = SystemUtil.getOsInfo();
        String emulator = getEmulator(connect.getCapabilities());
        return HostInfo.builder().hostName(connect.getHostName())
                .hostId(HostUtil.getHostId())
                .version(connect.getVersion())
                .uri(connect.getURI())
                .memory(connect.nodeInfo().memory)
                .cpu(connect.nodeInfo().cpus)
                .hypervisor(connect.getType())
                .arch(osInfo.getArch())
                .name(osInfo.getName())
                .emulator(emulator)
                .build();
    }
}
