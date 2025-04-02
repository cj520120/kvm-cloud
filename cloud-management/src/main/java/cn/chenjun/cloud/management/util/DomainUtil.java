package cn.chenjun.cloud.management.util;

import cn.chenjun.cloud.management.data.entity.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DomainUtil {

    public static String buildDiskXml(String tpl, Map<String, Object> systemConfig, GuestEntity guest, StorageEntity storage, VolumeEntity volume, int deviceId, String deviceType) {
        Map<String, Object> map = new HashMap<>();
        map.put("storage", ParamBuilder.buildStorageParam(storage));
        map.put("disk", ParamBuilder.buildDiskParam(volume, deviceId, deviceType));
        map.put("__SYS__", systemConfig);
        return TemplateUtil.create().render(tpl, map);
    }

    public static String buildCdXml(String tpl, Map<String, Object> systemConfig, StorageEntity storage, TemplateVolumeEntity volume) {
        Map<String, Object> map = new HashMap<>();
        map.put("storage", ParamBuilder.buildStorageParam(storage));
        map.put("template", ParamBuilder.buildTemplateVolumeParam(volume));
        map.put("__SYS__", systemConfig);
        return TemplateUtil.create().render(tpl, map);
    }

    public static String buildNetworkInterfaceXml(String tpl, Map<String, Object> systemConfig, NetworkEntity network, GuestNetworkEntity guestNetwork) {
        Map<String, Object> map = new HashMap<>();
        map.put("network", ParamBuilder.buildNetworkInterfaceParam(network, guestNetwork));
        map.put("__SYS__", systemConfig);
        return TemplateUtil.create().render(tpl, map);
    }

    public static String buildStorageXml(String tpl, Map<String, Object> systemConfig, StorageEntity storage) {
        Map<String, Object> map = new HashMap<>();
        map.put("storage", ParamBuilder.buildStorageParam(storage));
        map.put("__SYS__", systemConfig);
        return TemplateUtil.create().render(tpl, map);
    }

    public static String buildNetworkXml(String tpl, Map<String, Object> systemConfig, NetworkEntity network) {
        Map<String, Object> map = new HashMap<>();
        map.put("network", ParamBuilder.buildNetworkParam(network));
        map.put("__SYS__", systemConfig);
        return TemplateUtil.create().render(tpl, map);
    }

    public static String buildDomainXml(String tpl,
                                        Map<String, Object> systemConfig,
                                        GuestEntity guest,
                                        HostEntity host,
                                        SchemeEntity scheme,
                                        GuestVncEntity vnc,
                                        List<String> deviceXml
    ) {

        Map<String, Object> map = new HashMap<>();
        map.put("__SYS__", systemConfig);
        map.put("vm", ParamBuilder.buildVmParam(guest));
        map.put("host", ParamBuilder.buildHostParam(host));
        map.put("cpu", ParamBuilder.buildCpuParam(guest, scheme));
        map.put("vnc", ParamBuilder.buildVncParam(vnc));
        map.put("device", MapUtil.of("xml", String.join("\n", deviceXml)));
        return TemplateUtil.create().render(tpl, map);
    }
}
