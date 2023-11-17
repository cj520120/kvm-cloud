package cn.chenjun.cloud.agent.util;

import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.util.Constant;
import cn.hutool.core.io.resource.ResourceUtil;
import com.hubspot.jinjava.Jinjava;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainXmlUtil {
    public static final int MAX_DEVICE_COUNT = 5;
    public static final int MIN_DISK_DEVICE_ID = MAX_DEVICE_COUNT;
    public static final int MIN_NIC_DEVICE_ID = MIN_DISK_DEVICE_ID + MAX_DEVICE_COUNT;

    public static String buildDomainXml(String networkType, GuestStartRequest request) {
        Map<String, Object> map = new HashMap<>(0);
        map.put("name", request.getName());
        map.put("description", request.getDescription());
        map.put("memory", request.getOsMemory().getMemory());
        map.put("cpu", getCpuContext(request.getOsCpu()));
        map.put("emulator", request.getEmulator());
        map.put("cd", getCdContext(request.getOsCdRoom()));
        map.put("disks", getDisksContext(request.getBus(), request.getOsDisks()));
        map.put("networks", getNicsContext(networkType, request.getNetworkInterfaces()));
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

    public static String buildNicXml(String nicType, OsNic nic) {
        String xml = ResourceUtil.readUtf8Str("tpl/nic.xml");
        Jinjava jinjava = new Jinjava();
        Map<String, Object> map = new HashMap<>(0);
        map.put("nic", getNicContext(nicType, nic));
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
        map.put("path", cdRoom.getPath());
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
        String dev = "" + (char) ('a' + deviceId);
        Map<String, Object> map = new HashMap<>(0);
        map.put("bus", bus);
        map.put("dev", "vd" + dev);
        map.put("type", disk.getVolumeType());
        map.put("slot", String.format("0x%02x", deviceId));
        map.put("path", disk.getVolume());
        return map;
    }

    private static List<Map<String, Object>> getNicsContext(String type, List<OsNic> networks) {
        List<Map<String, Object>> list = new ArrayList<>(0);
        for (OsNic nic : networks) {
            list.add(getNicContext(type, nic));
        }
        return list;
    }

    private static Map<String, Object> getNicContext(String type, OsNic nic) {
        int deviceId = nic.getDeviceId() + MIN_NIC_DEVICE_ID;
        Map<String, Object> map = new HashMap<>(0);
        map.put("address", nic.getMac());
        map.put("type", nic.getDriveType());
        map.put("bridge", nic.getBridgeName());
        map.put("slot", String.format("0x%02x", deviceId));
        if (NetworkType.OPEN_SWITCH.equalsIgnoreCase(type)) {
            Map<String, Object> ovs = new HashMap<>(1);
            ovs.put("vlan", nic.getVlanId());
            map.put("ovs", ovs);
        }
        return map;
    }

    private static Map<String, Object> getVncContext(String password) {
        Map<String, Object> map = new HashMap<>(0);
        map.put("password", password);
        return map;
    }
}
