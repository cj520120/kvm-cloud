package cn.chenjun.cloud.agent.util;

import cn.chenjun.cloud.agent.operate.bean.DomainContext;
import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.RuntimeUtil;
import com.hubspot.jinjava.Jinjava;
import lombok.SneakyThrows;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
public class DomainXmlUtil {
    public static final int MAX_DEVICE_COUNT = 5;
    public static final int MIN_DISK_DEVICE_ID = MAX_DEVICE_COUNT;
    public static final int MIN_NIC_DEVICE_ID = MIN_DISK_DEVICE_ID + MAX_DEVICE_COUNT;
    public static DomainContext DOMAIN_CONTEXT;

    static {
        try {
            String capabilities = RuntimeUtil.execForStr("virsh domcapabilities");
            DOMAIN_CONTEXT = initDomainContext(capabilities);
        } catch (Exception err) {
            DOMAIN_CONTEXT = new DomainContext();
        }
        System.out.println(DOMAIN_CONTEXT);
    }


    private static String getNodeText(Document doc, String path) {
        Node node = doc.selectSingleNode(path);
        if (node != null) {
            return node.getText();
        }
        return null;
    }

    @SneakyThrows
    private static DomainContext initDomainContext(String xml) {
        try (StringReader sr = new StringReader(xml)) {
            DomainContext domainContext = new DomainContext();
            DomainContext.Loader loader = new DomainContext.Loader();
            domainContext.setLoader(loader);
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(sr);
            domainContext.setPath(getNodeText(doc, "/domainCapabilities/path"));
            domainContext.setDomain(getNodeText(doc, "/domainCapabilities/domain"));
            domainContext.setMachine(getNodeText(doc, "/domainCapabilities/machine"));
            domainContext.setArch(getNodeText(doc, "/domainCapabilities/arch"));
            List<Element> osEnumElementList = (List<Element>) doc.selectNodes("/domainCapabilities/os/enum");
            for (Element element : osEnumElementList) {
                if (Objects.equals("firmware", element.attributeValue("name"))) {
                    domainContext.setFirmware(element.selectSingleNode("value").getText());
                }
            }
            Element loaderElement = (Element) doc.selectSingleNode("/domainCapabilities/os/loader");
            if (loaderElement != null) {
                boolean supported = Objects.equals("yes", loaderElement.attributeValue("supported"));
                loader.setSupported(supported);
                if (supported) {
                    loader.setPath(loaderElement.selectSingleNode("value").getText());
                    loader.setInstall(new File(loader.getPath()).exists());
                    List<Element> loaderEnumElementList = (List<Element>) doc.selectNodes("/domainCapabilities/os/loader/enum");
                    for (Element element : loaderEnumElementList) {
                        if (Objects.equals("type", element.attributeValue("name"))) {
                            List<Element> valueElementList = (List<Element>) element.selectNodes("value");
                            loader.setType(valueElementList.stream().map(Element::getText).collect(Collectors.toList()));

                        } else if (Objects.equals("secure", element.attributeValue("name"))) {
                            List<Element> valueElementList = (List<Element>) element.selectNodes("value");
                            loader.setSecure(valueElementList.stream().map(Element::getText).map(val -> Objects.equals(val, "yes")).isParallel());
                        }
                    }
                }
            }
            return domainContext;
        }
    }


    public static String buildDomainXml(GuestStartRequest request) {
        Map<String, Object> map = new HashMap<>(0);
        map.put("name", request.getName());
        map.put("description", request.getDescription());
        map.put("memory", request.getOsMemory().getMemory());
        map.put("cpu", getCpuContext(request.getOsCpu()));
        map.put("emulator", request.getEmulator());
        map.put("cd", getCdContext(request.getOsCdRoom()));
        map.put("disks", getDisksContext(request.getBus(), request.getOsDisks()));
        map.put("networks", getBatchNicContext(request.getNetworkInterfaces()));
        map.put("vnc", getVncContext(request.getVncPassword()));
        String xml = ResourceUtil.readUtf8Str("tpl/domain.xml");
        Jinjava jinjava = new Jinjava();
        return jinjava.render(xml, map);
    }

    public static String buildCdXml(OsCdRoom cdRoom) {
        String xml = ResourceUtil.readUtf8Str("tpl/cd.xml");
        Jinjava jinjava = new Jinjava();
        Map<String, Object> map = new HashMap<>(0);
        map.put("cd", getCdContext(cdRoom));
        return jinjava.render(xml, map);
    }

    public static String buildDiskXml(String busType, OsDisk disk) {
        String xml = ResourceUtil.readUtf8Str("tpl/disk.xml");
        Jinjava jinjava = new Jinjava();

        Map<String, Object> map = new HashMap<>(0);
        map.put("disk", getDiskContext(busType, disk));

        return jinjava.render(xml, map);
    }

    public static String buildNicXml(OsNic nic) {
        String xml = ResourceUtil.readUtf8Str("tpl/nic.xml");
        Jinjava jinjava = new Jinjava();
        Map<String, Object> map = new HashMap<>(0);
        map.put("nic", getNicContext(nic));
        return jinjava.render(xml, map);
    }

    private static Map<String, Object> getCpuContext(OsCpu cpu) {
        Map<String, Object> map = new HashMap<>(0);
        map.put("number", cpu.getNumber());
        map.put("socket", cpu.getSocket());
        map.put("core", cpu.getCore());
        map.put("thread", cpu.getThread());
        map.put("share", cpu.getShare());
        return map;
    }

    private static Map<String, Object> getCdContext(OsCdRoom cdRoom) {
        Map<String, Object> map = new HashMap<>(0);
        map.put("type", "file");
        Volume volume = cdRoom.getVolume();
        if (volume != null) {
            Map<String, Object> storage = new HashMap<>(0);
            storage.put("name", volume.getStorage().getName());
            storage.put("param", volume.getStorage().getParam());
            storage.put("path", volume.getStorage().getMountPath());
            storage.put("type", volume.getStorage().getType());
            map.put("storage", storage);
            map.put("name", volume.getName());
            switch (volume.getStorage().getType()) {
                case Constant.StorageType.GLUSTERFS:
                    map.put("type", "network");
                    break;
                case Constant.StorageType.NFS:
                    map.put("type", "file");
                    break;
                default:
                    throw new CodeException(ErrorCode.BASE_STORAGE_ERROR, "不支持的存储池类型");

            }
        }
        return map;
    }

    private static List<Map<String, Object>> getDisksContext(String bus, List<OsDisk> disks) {
        List<Map<String, Object>> list = new ArrayList<>(0);
        for (int i = 0; i < disks.size(); i++) {
            OsDisk disk = disks.get(i);
            Map<String, Object> map = getDiskContext(i == 0 ? bus : Constant.DiskBus.VIRTIO, disk);
            list.add(map);
        }
        return list;
    }

    private static Map<String, Object> getDiskContext(String bus, OsDisk disk) {
        int deviceId = disk.getDeviceId() + MIN_DISK_DEVICE_ID;
        Map<String, Object> storage = new HashMap<>(0);
        Volume volume = disk.getVolume();
        Storage volumeStorage = volume.getStorage();
        storage.put("name", volumeStorage.getName());
        storage.put("param", volumeStorage.getParam());
        storage.put("path", volumeStorage.getMountPath());
        storage.put("type", volumeStorage.getType());
        String dev = "" + (char) ('a' + deviceId);
        Map<String, Object> map = new HashMap<>(0);
        map.put("name", volume.getName());
        map.put("bus", bus);
        map.put("dev", "vd" + dev);
        map.put("volumeType", volume.getType());
        map.put("slot", String.format("0x%02x", deviceId));
        map.put("storage", storage);
        switch (volumeStorage.getType()) {
            case Constant.StorageType.GLUSTERFS:
                map.put("type", "network");
                break;
            case Constant.StorageType.NFS:
                map.put("type", "file");
                break;
            default:
                throw new CodeException(ErrorCode.BASE_STORAGE_ERROR, "不支持的存储池类型");

        }
        return map;
    }

    private static List<Map<String, Object>> getBatchNicContext(List<OsNic> networks) {
        List<Map<String, Object>> list = new ArrayList<>(0);
        for (OsNic nic : networks) {
            list.add(getNicContext(nic));
        }
        return list;
    }

    private static Map<String, Object> getNicContext(OsNic nic) {
        int deviceId = nic.getDeviceId() + MIN_NIC_DEVICE_ID;
        Map<String, Object> map = new HashMap<>(0);
        map.put("address", nic.getMac());
        map.put("type", nic.getDriveType());
        map.put("bridge", nic.getBridgeName());
        map.put("slot", String.format("0x%02x", deviceId));
        map.put("network", nic.getPoolId());
//        if (Objects.equals(nic.getBridgeType(), Constant.NetworkBridgeType.OPEN_SWITCH)) {
//            Map<String, Object> ovs = new HashMap<>(1);
//            ovs.put("vlan", nic.getVlanId());
//            map.put("ovs", ovs);
//        }
//        if (nic.getVlanId() > 0) {
//            map.put("portgroup", "no-vlan");
//        } else {
//            map.put("portgroup", "vlan-" + nic.getVlanId());
//        }

        return map;
    }

    private static Map<String, Object> getVncContext(String password) {
        Map<String, Object> map = new HashMap<>(0);
        map.put("password", password);
        return map;
    }


}
