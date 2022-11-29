package cn.roamblue.test;

import cn.roamblue.cloud.agent.operate.StorageOperate;
import cn.roamblue.cloud.agent.operate.impl.StorageOperateImpl;
import cn.roamblue.cloud.common.agent.StorageRequest;
import cn.roamblue.cloud.common.util.Command;
import cn.roamblue.cloud.common.util.StorageType;
import org.libvirt.Connect;

import java.util.HashMap;
import java.util.Map;

public class StorageTest {
    public static void main(String[] args) throws Exception {
        Connect connect = new Connect("qemu:///system");
        createStorage(connect);
        destroyStorage(connect);

    }

    public static void createStorage(Connect connect) throws Exception {
        StorageOperate operate = new StorageOperateImpl();
        String name = "TEST_NFS";
        Map<String, Object> param = new HashMap<>();
        param.put("uri", "192.168.1.69");
        param.put("path", "/data/nfs");
        param.put("mount", "/mnt/TEST_NFS");
        StorageRequest request = StorageRequest.builder()
                .command(Command.Storage.CREATE)
                .name(name)
                .type(StorageType.NFS)
                .param(param)
                .build();

        Object object = operate.create(connect, request);
        System.out.println(object);
    }

    public static void destroyStorage(Connect connect) throws Exception {
        StorageOperate operate = new StorageOperateImpl();
        operate.destroy(connect, "TEST_NFS");
    }
}
