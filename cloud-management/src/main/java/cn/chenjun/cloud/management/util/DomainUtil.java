package cn.chenjun.cloud.management.util;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.BootstrapType;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.*;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainUtil {

    public static final int MAX_DEVICE_COUNT = 5;
    public static final int MIN_DISK_DEVICE_ID = MAX_DEVICE_COUNT;
    public static final int MIN_NIC_DEVICE_ID = MIN_DISK_DEVICE_ID + MAX_DEVICE_COUNT;


    public static String buildDiskXml(String tpl, Map<String, Object> sysconfig, GuestEntity guest, StorageEntity storage, VolumeEntity volume, GuestDiskEntity guestDisk) {
        Map<String, Object> map = new HashMap<>();
        map.put("storage", buildStorageParam(storage));
        map.put("disk", buildDiskParam(guest, volume, guestDisk));
        map.put("__SYS__", sysconfig);
        return TemplateUtil.create().render(tpl, map);
    }

    public static String buildCdXml(String tpl, Map<String, Object> sysconfig, StorageEntity storage, TemplateVolumeEntity volume) {
        Map<String, Object> map = new HashMap<>();
        map.put("storage", buildStorageParam(storage));
        map.put("template", buildTemplateVolumeParam(volume));
        map.put("__SYS__", sysconfig);
        return TemplateUtil.create().render(tpl, map);
    }

    public static String buildNetworkInterfaceXml(String tpl, Map<String, Object> sysconfig, NetworkEntity network, GuestNetworkEntity guestNetwork) {
        Map<String, Object> map = new HashMap<>();
        map.put("network", buildNetworkInterfaceParam(network, guestNetwork));
        map.put("__SYS__", sysconfig);
        return TemplateUtil.create().render(tpl, map);
    }

    public static String buildStorageXml(String tpl, Map<String, Object> sysconfig, StorageEntity storage) {
        Map<String, Object> map = new HashMap<>();
        map.put("storage", buildStorageParam(storage));
        map.put("__SYS__", sysconfig);
        return TemplateUtil.create().render(tpl, map);
    }

    public static String buildNetworkXml(String tpl, Map<String, Object> sysconfig, NetworkEntity network) {
        Map<String, Object> map = new HashMap<>();
        map.put("network", buildNetworkParam(network));
        map.put("__SYS__", sysconfig);
        return TemplateUtil.create().render(tpl, map);
    }

    public static String buildDomainXml(String tpl,
                                        Map<String, Object> sysconfig,
                                        GuestEntity guest,
                                        HostEntity host,
                                        SchemeEntity scheme,
                                        GuestVncEntity vnc,
                                        List<String> deviceXml
    ) {

        Map<String, Object> map = new HashMap<>();
        map.put("__SYS__", sysconfig);
        map.put("vm", buildVmParam(guest));
        map.put("host", buildHostParam(host));
        map.put("cpu", buildCpuParam(guest, scheme));
        map.put("vnc", buildVncParam(vnc));
        map.put("device", MapUtil.of("xml", String.join("\n", deviceXml)));
        return TemplateUtil.create().render(tpl, map);
    }

    private static Map<String, Object> buildCpuParam(GuestEntity guest, SchemeEntity scheme) {
        Map<String, Object> map = new HashMap<>(0);
        map.put("number", guest.getCpu());
        map.put("share", guest.getShare());
        if (scheme != null) {
            map.put("socket", scheme.getSockets());
            map.put("core", scheme.getCores());
            map.put("thread", scheme.getThreads());
        }
        return map;
    }

    private static Map<String, Object> buildVncParam(GuestVncEntity guestVncEntity) {
        Map<String, Object> map = new HashMap<>(0);
        if (guestVncEntity != null) {
            map.put("password", guestVncEntity.getPassword());
        }
        map.put("type", "vnc");
        return map;
    }

    public static Map<String, Object> buildVmParam(GuestEntity guest) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", guest.getName());
        map.put("description", guest.getDescription());
        map.put("memory", guest.getMemory());
        switch (guest.getBootstrapType()) {
            case BootstrapType.UEFI:
                map.put("bootstrapType", "uefi");
                break;
            default:
                map.put("bootstrapType", "bios");
                break;

        }
        return map;
    }

    public static Map<String, Object> buildHostParam(HostEntity host) {
        Map<String, Object> map = new HashMap<>();
        map.put("emulator", host.getEmulator());
        map.put("arch", host.getArch());
        return map;
    }

    public static Map<String, Object> buildNetworkParam(NetworkEntity network) {

        Map<String, Object> map = new HashMap<>();
        map.put("name", network.getPoolId());
        map.put("uuid", network.getPoolId());
        map.put("description", network.getName());
        map.put("network", network.getName());
        map.put("bridge", network.getBridge());
        map.put("vlanId", network.getVlanId());
        return map;
    }

    public static Map<String, Object> buildNetworkInterfaceParam(NetworkEntity network, GuestNetworkEntity guestNetwork) {
        int deviceId = guestNetwork.getDeviceId() + MIN_NIC_DEVICE_ID;
        Map<String, Object> map = new HashMap<>();
        map.put("mac", guestNetwork.getMac());
        map.put("type", guestNetwork.getDriveType());
        map.put("network", network.getPoolId());
        map.put("slot", String.format("0x%02x", deviceId));
        return map;
    }

    private static Map<String, Object> buildStorageParam(StorageEntity storage) {
        Map<String, Object> map = new HashMap<>();
        if (storage != null) {
            Map<String, Object> param = GsonBuilderUtil.create().fromJson(storage.getParam(), new TypeToken<Map<String, Object>>() {
            }.getType());
            map.put("name", storage.getName());
            map.put("mount", storage.getMountPath());
            map.put("type", storage.getType());
            map.put("param", param);
        }
        return map;
    }

    private static Map<String, Object> buildTemplateVolumeParam(TemplateVolumeEntity templateVolume) {
        Map<String, Object> map = new HashMap<>();
        if (templateVolume != null) {
            map.put("name", templateVolume.getName());
        }
        return map;
    }

    private static Map<String, Object> buildDiskParam(GuestEntity guest, VolumeEntity volume, GuestDiskEntity guestDisk) {
        int deviceId = guestDisk.getDeviceId() + MIN_DISK_DEVICE_ID;
        String bus = guestDisk.getDeviceId() == 0 ? guest.getBusType() : Constant.DiskBus.VIRTIO;
        String deviceName = "vd" + (char) ('a' + guestDisk.getDeviceId());
        Map<String, Object> map = new HashMap<>();
        map.put("device", deviceName);
        map.put("type", volume.getType());
        map.put("slot", String.format("0x%02x", deviceId));
        map.put("bus", bus);
        map.put("name", volume.getName());
        return map;
    }
}
