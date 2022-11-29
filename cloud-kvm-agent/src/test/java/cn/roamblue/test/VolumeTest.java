package cn.roamblue.test;

import cn.roamblue.cloud.agent.operate.VolumeOperate;
import cn.roamblue.cloud.agent.operate.impl.VolumeOperateImpl;
import cn.roamblue.cloud.common.agent.VolumeRequest;
import org.libvirt.Connect;

public class VolumeTest {
    public static void main(String[] args) throws Exception {
        Connect connect = new Connect("qemu:///system");
        StorageTest.createStorage(connect);
        {
            VolumeRequest.CreateVolume request = VolumeRequest.CreateVolume.builder()
                    .targetStorage("TEST_NFS")
                    .targetVolume("/mnt/TEST_NFS/TEST_VOL_1")
                    .targetName("TEST_VOL_1")
                    .targetType("raw")
                    .targetSize(1024 * 1024)
                    .build();
            createVolume(connect, request);
        }
        {

            VolumeRequest.CreateVolume request = VolumeRequest.CreateVolume.builder()
                    .parentStorage("TEST_NFS")
                    .parentVolume("/mnt/TEST_NFS/TEST_VOL_1")
                    .parentType("raw")
                    .targetStorage("TEST_NFS")
                    .targetVolume("/mnt/TEST_NFS/TEST_VOL_2")
                    .targetName("TEST_VOL_2")
                    .targetType("qcow2")
                    .targetSize(1024 * 1024)
                    .build();
            createVolume(connect, request);
        }

        {
            VolumeRequest.DestroyVolume destroy = VolumeRequest.DestroyVolume.builder()
                    .sourceStorage("TEST_NFS")
                    .sourceVolume("/mnt/TEST_NFS/TEST_VOL_2")
                    .sourceType("qcow2")
                    .build();
            VolumeTest.destroyVolume(connect, destroy);
        }
        {
            VolumeRequest.DestroyVolume destroy = VolumeRequest.DestroyVolume.builder()
                    .sourceStorage("TEST_NFS")
                    .sourceVolume("/mnt/TEST_NFS/TEST_VOL_1")
                    .sourceType("raw")
                    .build();
            VolumeTest.destroyVolume(connect, destroy);
        }
    }

    public static void createVolume(Connect connect, VolumeRequest.CreateVolume request) throws Exception {
        VolumeOperate operate = new VolumeOperateImpl();

        operate.create(connect, request);
    }
    public static void destroyVolume(Connect connect, VolumeRequest.DestroyVolume request) throws Exception {
        VolumeOperate operate = new VolumeOperateImpl();

        operate.destroy(connect, request);
    }
}
