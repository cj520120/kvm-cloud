package cn.chenjun.cloud.test;

import cn.chenjun.cloud.agent.util.NetworkType;
import cn.chenjun.cloud.agent.util.DomainXmlUtil;
import cn.chenjun.cloud.common.bean.GuestStartRequest;
import cn.chenjun.cloud.common.bean.OsNic;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;

public class DomainXmlTest {
    static String json="{\"name\":\"VM-hNxhKWIIAE\",\"description\":\"TEST\",\"emulator\":\"/usr/libexec/qemu-kvm\",\"osMemory\":{\"memory\":2097152},\"osCpu\":{\"number\":2,\"socket\":0,\"core\":0,\"thread\":0,\"share\":0},\"osCdRoom\":{},\"bus\":\"virtio\",\"osDisks\":[{\"name\":\"VM-hNxhKWIIAE\",\"deviceId\":0,\"volume\":\"/mnt/0981d4d911fa4f1c9f5a4eb396529ff1/3c550ab4ae17485c954c3b9c6f093242\",\"volumeType\":\"qcow2\"}],\"networkInterfaces\":[{\"name\":\"VM-hNxhKWIIAE\",\"driveType\":\"virtio\",\"deviceId\":0,\"mac\":\"00:23:D3:7B:85:35\",\"bridgeName\":\"br0\",\"vlanId\":10}],\"vncPassword\":\"xe5OxJLL\"}";
    public static void main(String[] args) {
        GuestStartRequest request= GsonBuilderUtil.create().fromJson(json,GuestStartRequest.class);
        System.out.println(DomainXmlUtil.buildDomainXml(NetworkType.OPEN_SWITCH,request));

        System.out.println(DomainXmlUtil.buildCdXml(request.getOsCdRoom()));
        System.out.println(DomainXmlUtil.buildDiskXml(Constant.DiskBus.VIRTIO,request.getOsDisks().get(0)));
        System.out.println(DomainXmlUtil.buildNicXml(NetworkType.OPEN_SWITCH,request.getNetworkInterfaces().get(0)));
        System.out.println(DomainXmlUtil.buildNicXml(NetworkType.OPEN_SWITCH, GsonBuilderUtil.create().fromJson("{\"name\":\"VM-hNxhKWIIAE\",\"driveType\":\"virtio\",\"deviceId\":1,\"mac\":\"00:24:E9:AD:D6:3D\",\"bridgeName\":\"br0\",\"vlanId\":0}", OsNic.class)));
    }
}
