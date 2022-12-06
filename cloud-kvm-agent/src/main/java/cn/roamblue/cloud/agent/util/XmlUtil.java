package cn.roamblue.cloud.agent.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.MD5;
import cn.roamblue.cloud.common.agent.VmModel;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.util.List;

/**
 * @author chenjun
 */
public final class XmlUtil {

    private static String emulator;
    private XmlUtil() {

    }
    public static void initEmulator(String xml) throws SAXException, DocumentException {

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
    }
    public static String toXml(String cdRoom) {
        StringBuilder sb = new StringBuilder();
        sb.append("<disk type='file' device='cdrom'>");
        sb.append("<driver name='qemu'/>");
        if (!StringUtils.isEmpty(cdRoom)) {
            sb.append("<source file='").append(cdRoom).append("'/>");
        }
        sb.append("<target dev='hdc' bus='ide'/>");
        sb.append("<readonly/>");
        sb.append("<address type='drive' controller='0' bus='0' target='0' unit='0'/>");
        sb.append("</disk>");
        return sb.toString();
    }

    public static String toXml(VmModel.Cpu cpu) {
        StringBuilder sb = new StringBuilder();
        sb.append("<vcpu placement='static'>").append(cpu.getCpu()).append("</vcpu>");
        if (cpu.getSpeed() > 0) {
            sb.append("<cputune><shares>").append(cpu.getSpeed()).append("</shares></cputune>");
        }
        if (cpu.getSocket() > 0 && cpu.getCore() > 0 && cpu.getThreads() > 0) {
            sb.append("<cpu mode='host-passthrough' check='partial'><model fallback='allow'>Nehalem</model><topology sockets='").append(cpu.getSocket()).append("' cores='").append(cpu.getCore()).append("' threads='").append(cpu.getThreads()).append("'/></cpu>");
        } else {
            sb.append("<cpu mode='host-passthrough' check='partial'><model fallback='allow'>Nehalem</model><topology sockets='2' cores='4' threads='8'/></cpu>");
        }
        return sb.toString();
    }

    public static String toXml(VmModel.Memory memory) {
        StringBuilder sb = new StringBuilder();
        sb.append("<memory unit='KiB'>").append(memory.getMemory()).append("</memory><currentMemory unit='KiB'>").append(memory.getMemory()).append("</currentMemory>");
        return sb.toString();
    }

    public static String toXml(VmModel.Disk disk) {
        if (disk.getDevice() >= 10) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "超过最大磁盘数量");
        }
        StringBuilder sb = new StringBuilder();
        String dev = "vd" + (char) ('a' + disk.getDevice());
        sb.append("<disk type='file' device='disk'>");
        sb.append("<driver name='qemu' type='qcow2' cache='none'/>");
        sb.append("<source file='").append(disk.getPath()).append("'/>");
        sb.append("<target dev='").append(dev).append("' bus='virtio'/>");
        sb.append("<address type='pci' domain='0x0000' bus='0x00' slot='" + String.format("0x%02x", disk.getDevice() + 5) + "' function='0x0'/>");

        sb.append("<alias name='disk-").append(disk.getDevice()).append("'/>");
        sb.append("</disk>");
        return sb.toString();
    }

    public static String toXml(VmModel.Network network) {
        if (network.getDevice() >= 10) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "超过最大磁盘数量");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<interface type='bridge'>")
                .append("<mac address='").append(network.getMac()).append("'/>")
                .append("<source bridge='").append(network.getSource()).append("'/>")
                .append("<model type='" + network.getDriver() + "'/>")
                .append("<address type='pci' domain='0x0000' bus='0x00' slot='").append(String.format("0x%02x", network.getDevice() + 20)).append("' function='0x0'/>")
                .append("<link state='up'/>")
                .append("</interface>");
        return sb.toString();
    }

    public static String toXml(VmModel instance) {
        StringBuilder sb = new StringBuilder();
        sb.append("<domain type='kvm'>");
        sb.append("<name>").append(instance.getName()).append("</name>");
        sb.append("<uuid>").append(MD5.create().digestHex(instance.getId() + "")).append("</uuid>");
        if (!StringUtils.isEmpty(instance.getDescription())) {
            sb.append("<description>").append(instance.getDescription()).append("</description>");
        }
        sb.append(toXml(instance.getCpu()));
        sb.append(toXml(instance.getMemory()));
        sb.append("<resource><partition>/machine</partition></resource>");
        sb.append("<sysinfo type='smbios'>");
        sb.append("<system>");
        sb.append("<entry name='product'>Roamblue</entry>");
        sb.append("</system>");
        sb.append("</sysinfo>");
        sb.append("<os>");
        sb.append("<type>hvm</type>");
        sb.append("<boot dev='hd'/>");
        sb.append("<boot dev='cdrom'/>");
        sb.append("<bootmenu enable ='yes' timeout='5000'/>");
        sb.append("</os>");
        sb.append("<features><pae/><acpi/><apic/><hap/><privnet/></features>");
        sb.append("<clock offset='localtime'></clock>");
        //控制周期
        sb.append("<on_poweroff>destroy</on_poweroff>");
        sb.append("<on_reboot>restart</on_reboot>");
        sb.append("<on_crash>destroy</on_crash>");
        sb.append("<devices>");
        if(FileUtil.exist(emulator)){
            sb.append("<emulator>"+emulator+"</emulator>");
        }
        else if (FileUtil.exist("/usr/libexec/qemu-kvm")) {
            //centos 系统（已测试centos7）
            sb.append("<emulator>/usr/libexec/qemu-kvm</emulator>");
        } else if (FileUtil.exist("/usr/bin/qemu-system-x86_64")) {
            //ubuntu 系统 (已测试ubuntu 18.04)
            sb.append("<emulator>/usr/bin/qemu-system-x86_64</emulator>");
        } else {
            throw new CodeException(ErrorCode.VM_COMMAND_ERROR, "未找到有效的qemu路径");
        }
        //光盘
        sb.append(toXml(instance.getCdRoom()));
        //主磁盘
        sb.append("<disk type='file' device='disk'>");
        sb.append("<driver name='qemu' type='qcow2' cache='none'/>");
        sb.append("<source file='").append(instance.getRoot().getPath()).append("'/>");
        sb.append("<target dev='vda' bus='").append(instance.getRoot().getDriver()).append("'/>");
        if ("virtio".equalsIgnoreCase(instance.getRoot().getDriver())) {
            sb.append("<address type='pci' domain='0x0000' bus='0x00' slot='").append(String.format("0x%02x", 5)).append("' function='0x0'/>");
        } else if ("ide".equalsIgnoreCase(instance.getRoot().getDriver())) {
            //光盘占用bus=0
            sb.append("<address type='drive' controller='0' bus='1' target='0' unit='0'/>");
        } else {
            //scsi sata 硬盘格式
            sb.append("<address type='drive' controller='0' bus='0' target='0' unit='0'/>");
        }
        sb.append("</disk>");
        //扩展磁盘
        List<VmModel.Disk> disks = instance.getDisks();
        if (disks != null && !disks.isEmpty()) {
            for (VmModel.Disk disk : disks) {
                sb.append(toXml(disk));
            }
        }
        //添加虚拟机通讯控制接口
        sb.append("<channel type='unix'>");
        sb.append("<source mode='bind' path='/var/lib/libvirt/qemu/").append(instance.getName()).append(".org.qemu.guest_agent.0'/>");
        sb.append("<target type='virtio' name='org.qemu.guest_agent.0'/>");
        sb.append("</channel>");
        //添加鼠标
        sb.append("<input type='tablet' bus='usb'/>");
        sb.append("<input type='mouse' bus='ps2'/>");
        //添加键盘
        sb.append("<input type='keyboard' bus='ps2'/>");
        //添加video
        sb.append("<video><model type='cirrus' vram='16384' heads='1' primary='yes'/></video>");
        //串口终端
        sb.append("<serial type='pty'><target port='0'/></serial>");
        //控制台
        sb.append("<console type='pty'><target type='serial' port='0'/></console>");
        //添加vnc
        if (StringUtils.isEmpty(instance.getPassword())) {
            sb.append("<graphics type='vnc' port='-1' autoport='yes' keymap='en-us'  listen='0.0.0.0'/>");
        } else {
            sb.append("<graphics type='vnc' port='-1' autoport='yes' keymap='en-us' passwd='").append(instance.getPassword()).append("' listen='0.0.0.0'/>");
        }
        //添加网卡
        List<VmModel.Network> networks = instance.getNetwroks();
        if (networks != null) {
            for (VmModel.Network network : networks) {
                sb.append(toXml(network));
            }
        }
        //增加监控
        sb.append("<memballoon model='virtio'><stats period='10'/></memballoon>");
        sb.append("</devices>");
        sb.append("</domain>");
        return sb.toString();
    }

    public static int getVnc(String xml) throws DocumentException, SAXException {
        try (StringReader sr = new StringReader(xml)) {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(sr);
            String path = "/domain/devices/graphics";
            Element node = (Element) doc.selectSingleNode(path);
            return Integer.parseInt(node.attribute("port").getValue());
        }
    }

    public static String getVncPassword(String xml) throws DocumentException, SAXException {
        try (StringReader sr = new StringReader(xml)) {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(sr);
            String path = "/domain/devices/graphics";
            Element node = (Element) doc.selectSingleNode(path);
            Attribute attribute = node.attribute("passwd");
            return attribute == null ? "" : attribute.getStringValue();
        }
    }
}
