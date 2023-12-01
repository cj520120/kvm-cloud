package cn.chenjun.cloud.test;

import cn.chenjun.cloud.agent.util.DomainXmlUtil;
import cn.chenjun.cloud.agent.util.NetworkType;
import cn.chenjun.cloud.common.bean.*;
import cn.hutool.http.HttpUtil;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;

import java.util.*;

public class OsTest {
    public static void main(String[] args) {
        String json="{\"name\":\"VM-NTmsXYEgy\",\"description\":\"gluster\",\"emulator\":\"/usr/libexec/qemu-kvm\",\"osMemory\":{\"memory\":2097152},\"osCpu\":{\"number\":2,\"socket\":0,\"core\":0,\"thread\":0,\"share\":0},\"osCdRoom\":{},\"bus\":\"virtio\",\"osDisks\":[{\"name\":\"VM-NTmsXYEgy\",\"deviceId\":0,\"volume\":{\"name\":\"0951d4bf-135b-4e8c-8749-cb9addea3987\",\"path\":\"/mnt/27b21e06aa444b2e924faa1b850275b8/0951d4bf-135b-4e8c-8749-cb9addea3987\",\"type\":\"qcow2\",\"storage\":{\"name\":\"27b21e06aa444b2e924faa1b850275b8\",\"type\":\"glusterfs\",\"mountPath\":\"/mnt/27b21e06aa444b2e924faa1b850275b8\",\"param\":{\"path\":\"v1\",\"uri\":\"192.168.1.54\"}}}}],\"networkInterfaces\":[{\"name\":\"VM-NTmsXYEgy\",\"driveType\":\"virtio\",\"deviceId\":0,\"mac\":\"00:04:EA:62:A8:A9\",\"bridgeName\":\"br0\",\"vlanId\":0}],\"vncPassword\":\"oIW5iOQ4\"}";
//        String json="{\"name\":\"VM-eMVBnWcS\",\"description\":\"TEST_002\",\"emulator\":\"/usr/libexec/qemu-kvm\",\"osMemory\":{\"memory\":1048576},\"osCpu\":{\"number\":1,\"socket\":0,\"core\":0,\"thread\":0,\"share\":0},\"osCdRoom\":{},\"bus\":\"virtio\",\"osDisks\":[{\"name\":\"VM-eMVBnWcS\",\"deviceId\":0,\"volume\":{\"name\":\"8d4659fc-6673-47ef-9bf2-7b23d12a7e7e\",\"path\":\"/mnt/0981d4d911fa4f1c9f5a4eb396529ff1/8d4659fc-6673-47ef-9bf2-7b23d12a7e7e\",\"type\":\"qcow2\",\"storage\":{\"name\":\"0981d4d911fa4f1c9f5a4eb396529ff1\",\"type\":\"nfs\",\"mountPath\":\"/mnt/0981d4d911fa4f1c9f5a4eb396529ff1\",\"param\":{\"path\":\"/data/nfs\",\"uri\":\"192.168.1.90\"}}}}],\"networkInterfaces\":[{\"name\":\"VM-eMVBnWcS\",\"driveType\":\"virtio\",\"deviceId\":0,\"mac\":\"00:0D:ED:E3:A4:04\",\"bridgeName\":\"br0\",\"vlanId\":200}],\"vncPassword\":\"LKAWROCL\"}";
        GuestStartRequest request= GsonBuilderUtil.create().fromJson(json, GuestStartRequest.class);
        request.getOsCdRoom().setVolume(request.getOsDisks().get(0).getVolume());
        String xml = DomainXmlUtil.buildDomainXml(NetworkType.OPEN_SWITCH, request);
        System.out.println(xml);
    }

}
