package cn.roamblue.test;

import cn.hutool.http.HttpUtil;
import cn.roamblue.cloud.common.bean.*;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.common.util.Constant;

import java.util.*;

public class OsTest {
    public static void main(String[] args) {
        getHostInfo();
        createNetwork();
        createStorage();
        startOs(3,5, Constant.DiskBus.IDE);
        destroyOs();
        destroyVolume(5);
        destroyStorage();
        destroyNetwork();
    }
   public static void getHostInfo(){

       Map<String,Object> map=new HashMap<>();
       map.put("command",Constant.Command.HOST_INFO);
       map.put("data", "");
       System.out.println(HttpUtil.post("http://192.168.1.69:8081/api/operate",map));
   }
    public static void createStorage(){
        String name = "TEST_NFS";
        Map<String, Object> param = new HashMap<>();
        param.put("uri", "192.168.1.69");
        param.put("path", "/data/nfs");
        param.put("mount", "/mnt/TEST_NFS");
        StorageCreateRequest request = StorageCreateRequest.builder()
                .name(name)
                .type(Constant.StorageType.NFS)
                .param(param)
                .build();
        Map<String,Object> map=new HashMap<>();
        map.put("command",Constant.Command.STORAGE_CREATE);
        map.put("data", GsonBuilderUtil.create().toJson(request));
        System.out.println(HttpUtil.post("http://192.168.1.69:8081/api/operate",map));
    }

    public static void destroyStorage(){
        String name = "TEST_NFS";
        StorageDestroyRequest request = StorageDestroyRequest.builder()
                .name(name)
                .build();
        Map<String,Object> map=new HashMap<>();
        map.put("command",Constant.Command.STORAGE_DESTROY);
        map.put("data", GsonBuilderUtil.create().toJson(request));
        System.out.println(HttpUtil.post("http://192.168.1.69:8081/api/operate",map));
    }
    public static void createNetwork(){
        BasicBridgeNetwork request=BasicBridgeNetwork.builder().bridge("br0").ip("192.168.1.69").geteway("192.168.1.1").nic("ens20").netmask("255.255.255.0").build();
        Map<String,Object> map=new HashMap<>();
        map.put("command",Constant.Command.NETWORK_CREATE_BASIC);
        map.put("data", GsonBuilderUtil.create().toJson(request));
        System.out.println(HttpUtil.post("http://192.168.1.69:8081/api/operate",map));
    }
    public static void destroyNetwork(){
        BasicBridgeNetwork request=BasicBridgeNetwork.builder().bridge("br0").ip("192.168.1.69").geteway("192.168.1.1").nic("ens20").netmask("255.255.255.0").build();
        Map<String,Object> map=new HashMap<>();
        map.put("command",Constant.Command.NETWORK_DESTROY_BASIC);
        map.put("data", GsonBuilderUtil.create().toJson(request));
        System.out.println(HttpUtil.post("http://192.168.1.69:8081/api/operate",map));
    }
    public static OsDisk createVolume(int index){
        List<String> volumeTypes = Arrays.asList(Constant.VolumeType.QCOW2, Constant.VolumeType.QCOW, Constant.VolumeType.RAW, Constant.VolumeType.VDI, Constant.VolumeType.VPC, Constant.VolumeType.VMDK);
        String volumeType = volumeTypes.get(index % volumeTypes.size());
        VolumeCreateRequest request = VolumeCreateRequest.builder()
                .targetStorage("TEST_NFS")
                .targetVolume("/mnt/TEST_NFS/" + "VOL_" + volumeType + "_" + index)
                .targetName("VOL_" + volumeType + "_" + index)
                .targetType(volumeType)
                .targetSize(1024L * 1024 * 1024 * 30)
                .build();
        Map<String,Object> map=new HashMap<>();
        map.put("command",Constant.Command.VOLUME_CREATE);
        map.put("data", GsonBuilderUtil.create().toJson(request));
        System.out.println(HttpUtil.post("http://192.168.1.69:8081/api/operate",map));
        return OsDisk.builder()
                .volume(request.getTargetVolume())
                .deviceId(index)
                .volumeType(volumeType).build();
    }
    public static void destroyVolume(int number){
        for(int index=0;index<number;index++){
            List<String> volumeTypes = Arrays.asList(Constant.VolumeType.QCOW2, Constant.VolumeType.QCOW, Constant.VolumeType.RAW, Constant.VolumeType.VDI, Constant.VolumeType.VPC, Constant.VolumeType.VMDK);
            String volumeType = volumeTypes.get(index % volumeTypes.size());
            VolumeDestroyRequest request = VolumeDestroyRequest.builder()
                    .sourceStorage("TEST_NFS")
                    .sourceVolume("/mnt/TEST_NFS/" + "VOL_" + volumeType + "_" + index)
                    .sourceType(volumeType)
                    .build();
            Map<String,Object> map=new HashMap<>();
            map.put("command",Constant.Command.VOLUME_DESTROY);
            map.put("data", GsonBuilderUtil.create().toJson(request));
            System.out.println(HttpUtil.post("http://192.168.1.69:8081/api/operate",map));
        }
    }
    public static void startOs(int networkNumber,int diskNumber,String diskBus){
        List<OsDisk> osDisks = new ArrayList<>();
        for (int i = 0; i < diskNumber; i++) {
            osDisks.add(createVolume(i));
        }
        List<OsNic> networkInterfaces = new ArrayList<>();
        for (int i = 0; i < networkNumber; i++) {
            networkInterfaces.add(OsNic.builder().bridgeName("br0").mac(randomMacAddress()).deviceId(i).driveType("virtio").build());
        }
        GuestStartRequest request = GuestStartRequest.builder()
                .emulator("/usr/bin/qemu-system-x86_64")
                .name("VM_TEST")
                .description("测试虚拟机")
                .bus(diskBus)
                .osCpu(OsCpu.builder().number(8).core(0).socket(0).thread(0).share(500).build())
                .osMemory(OsMemory.builder().memory(1024 * 1024).build())
                .osCdRoom(OsCdRoom.builder()
                        .path("/mnt/TEST_NFS/CentOS-7-x86_64-Minimal-2003.iso")
                        .build())
                .osDisks(osDisks)
                .networkInterfaces(networkInterfaces)
                .vncPassword("123456")
                .build();
        Map<String,Object> map=new HashMap<>();
        map.put("command",Constant.Command.GUEST_START);
        map.put("data", GsonBuilderUtil.create().toJson(request));
        System.out.println(HttpUtil.post("http://192.168.1.69:8081/api/operate",map));
    }
    public static void destroyOs(){
        GuestDestroyRequest request= GuestDestroyRequest.builder().name("VM_TEST").build();
        Map<String,Object> map=new HashMap<>();
        map.put("command",Constant.Command.GUEST_DESTROY);
        map.put("data", GsonBuilderUtil.create().toJson(request));
        System.out.println(HttpUtil.post("http://192.168.1.69:8081/api/operate",map));
    }
    public static String randomMacAddress() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (sb.length() > 0) {
                sb.append(":");
            }

            Random random = new Random();
            int val = random.nextInt(256);
            String element = Integer.toHexString(val);
            if (element.length() < 2) {
                sb.append(0);
            }

            sb.append(element);
        }

        return "00:"+sb.toString();
    }
}
