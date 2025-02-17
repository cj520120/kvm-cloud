package cn.chenjun.cloud.agent.util;

import cn.chenjun.cloud.agent.config.ApplicationConfig;
import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.BootstrapType;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.io.resource.ResourceUtil;
import com.hubspot.jinjava.Jinjava;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * @author chenjun
 */
@Slf4j
public class DomainXmlUtil {
    public static final int MAX_DEVICE_COUNT = 5;
    public static final int MIN_DISK_DEVICE_ID = MAX_DEVICE_COUNT;
    public static final int MIN_NIC_DEVICE_ID = MIN_DISK_DEVICE_ID + MAX_DEVICE_COUNT;


    public static String buildDomainXml(GuestStartRequest request, ApplicationConfig config) {
        Map<String, Object> map = new HashMap<>(0);
        map.put("name", request.getName());
        map.put("description", request.getDescription());
        map.put("memory", request.getOsMemory().getMemory());
        map.put("cpu", getCpuContext(request.getOsCpu()));
        map.put("emulator", request.getEmulator());
        map.put("cd", getCdContext(request.getOsCdRoom(),config));
        map.put("disks", getDisksContext(request.getBus(), request.getOsDisks()));
        map.put("networks", getBatchNicContext(request.getNetworkInterfaces()));
        map.put("vnc", getVncContext(request.getVncPassword()));
        map.put("enableMemoryHugePages", request.isEnableMemoryHugePages());

        if (config.getMachine() != null && !ObjectUtils.isEmpty(config.getMachine().getName())) {
            Map<String, Object> machineMap = new HashMap<>(2);
            machineMap.put("name", config.getMachine().getName().toLowerCase());
            machineMap.put("arch", config.getMachine().getArch());
            map.put("machine", machineMap);
        }
        if (Objects.equals(request.getBootstrapType(), BootstrapType.UEFI)) {
            if (config.getUefi() == null || ObjectUtils.isEmpty(config.getUefi().getType()) || ObjectUtils.isEmpty(config.getUefi().getType())) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "该主机不支持UEFI");
            }
            Map<String, Object> uefiMap = new HashMap<>(2);
            uefiMap.put("type", config.getUefi().getType());
            uefiMap.put("path", config.getUefi().getPath());
            map.put("uefi", uefiMap);
        }
        String xml = ResourceUtil.readUtf8Str("tpl/domain.xml");
        Jinjava jinjava = TemplateUtil.create();
        return jinjava.render(xml, map);
    }

    public static String buildCdXml(OsCdRoom cdRoom,ApplicationConfig config) {
        String xml = ResourceUtil.readUtf8Str("tpl/cd.xml");
        Jinjava jinjava = TemplateUtil.create();
        Map<String, Object> map = new HashMap<>(0);
        map.put("cd", getCdContext(cdRoom,config));
        return jinjava.render(xml, map);
    }

    public static String buildDiskXml(String busType, OsDisk disk) {
        String xml = ResourceUtil.readUtf8Str("tpl/disk.xml");
        Jinjava jinjava = TemplateUtil.create();

        Map<String, Object> map = new HashMap<>(0);
        map.put("disk", getDiskContext(busType, disk));

        return jinjava.render(xml, map);
    }

    public static String buildNicXml(OsNic nic) {
        String xml = ResourceUtil.readUtf8Str("tpl/nic.xml");
        Jinjava jinjava = TemplateUtil.create();
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

    private static Map<String, Object> getCdContext(OsCdRoom cdRoom,ApplicationConfig config) {
        Map<String, Object> map = new HashMap<>(0);
        map.put("type", "file");
        if (config.getCd() != null && !ObjectUtils.isEmpty(config.getCd().getBus())) {
            map.put("bus", config.getCd().getBus());
        } else {
            map.put("bus", "ide");
        }
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
        return map;
    }

    private static Map<String, Object> getVncContext(String password) {
        Map<String, Object> map = new HashMap<>(0);
        map.put("password", password);
        map.put("type", "vnc");
        return map;
    }


}
