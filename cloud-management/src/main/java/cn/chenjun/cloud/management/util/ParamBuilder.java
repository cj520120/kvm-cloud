package cn.chenjun.cloud.management.util;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.*;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class ParamBuilder {
    public static Map<String, Object> buildCpuParam(GuestEntity guest, SchemeEntity scheme) {
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

    public static Map<String, Object> buildVncParam(String vncPassword) {
        Map<String, Object> map = new HashMap<>(0);
        if (vncPassword != null) {
            map.put("password", vncPassword);
        }
        return map;
    }

    public static Map<String, Object> buildVmParam(GuestEntity guest) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", guest.getName());
        map.put("description", guest.getDescription());
        map.put("memory", guest.getMemory());
        map.put("bootstrapType", guest.getBootstrapType());
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
//        int deviceId = guestNetwork.getDeviceId() + DomainUtil.MIN_NIC_DEVICE_ID;
        Map<String, Object> map = new HashMap<>();
        map.put("mac", guestNetwork.getMac());
        map.put("type", guestNetwork.getDriveType());
        map.put("network", network.getPoolId());
        map.put("deviceId", guestNetwork.getDeviceId());
        return map;
    }

    public static Map<String, Object> buildStorageParam(StorageEntity storage) {
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

    public static Map<String, Object> buildTemplateVolumeParam(TemplateVolumeEntity templateVolume) {
        Map<String, Object> map = new HashMap<>();
        if (templateVolume != null) {
            map.put("name", templateVolume.getName());
        }
        return map;
    }

    public static Map<String, Object> buildDiskParam(VolumeEntity volume, int deviceId, String deviceType) {
        String targetName;
        switch (deviceType) {
            case Constant.DiskDriveType.IDE:
                targetName = "hd" + (char) ('a' + deviceId + 1);
                break;
            case Constant.DiskDriveType.SCSI:
            case Constant.DiskDriveType.SATA:
                targetName = "sd" + (char) ('a' + deviceId);
                break;
            case Constant.DiskDriveType.VIRTIO:
                targetName = "vd" + (char) ('a' + deviceId);
                break;
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的磁盘驱动:" + deviceType);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", deviceId);
        map.put("target", targetName);
        map.put("type", volume.getType());
        map.put("bus", deviceType);
        map.put("name", volume.getName());
        return map;
    }
}
