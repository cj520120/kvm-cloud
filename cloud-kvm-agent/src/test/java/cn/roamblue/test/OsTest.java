package cn.roamblue.test;

import cn.roamblue.cloud.agent.operate.NetworkOperate;
import cn.roamblue.cloud.agent.operate.impl.NetworkOperateImpl;
import cn.roamblue.cloud.agent.operate.impl.OsOperateImpl;
import cn.roamblue.cloud.common.agent.NetworkRequest;
import cn.roamblue.cloud.common.agent.OsRequest;
import cn.roamblue.cloud.common.agent.VolumeRequest;
import cn.roamblue.cloud.common.util.Command;
import cn.roamblue.cloud.common.util.VolumeType;
import org.libvirt.Connect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OsTest {
    public static void main(String[] args) throws Exception {
        Connect connect = new Connect("qemu:///system");
        List<String> volumeTypes = Arrays.asList(VolumeType.QCOW2, VolumeType.QCOW, VolumeType.RAW, VolumeType.VDI, VolumeType.VPC, VolumeType.VMDK);
        List<String> diskBus = Arrays.asList(OsRequest.Disk.DiskBus.SCSI, OsRequest.Disk.DiskBus.IDE, OsRequest.Disk.DiskBus.VIRTIO);
        StorageTest.createStorage(connect);
        {
            NetworkOperate operate = new NetworkOperateImpl();
            NetworkRequest request = NetworkRequest.builder()
                    .command(Command.Network.CREATE_BASIC)
                    .basicBridge(NetworkRequest.BasicBridge.builder().bridge("br0").ip("192.168.1.69").geteway("192.168.1.1").nic("ens20").netmask("255.255.255.0").build())
                    .vlan(NetworkRequest.Vlan.builder().vlanId(100).bridge("vlan.100").ip("192.168.3.2").netmask("255.255.255.0").geteway("192.168.3.1").build())
                    .build();
            operate.createBasic(connect, request);
        }

        List<OsRequest.Disk> disks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String volumeType = volumeTypes.get(i % volumeTypes.size());
            VolumeRequest.CreateVolume request = VolumeRequest.CreateVolume.builder()
                    .targetStorage("TEST_NFS")
                    .targetVolume("/mnt/TEST_NFS/" + "VOL_" + volumeType + "_" + i)
                    .targetName("VOL_" + volumeType + "_" + i)
                    .targetType(volumeType)
                    .targetSize(1024L * 1024 * 1024 * 30)
                    .build();
            VolumeTest.createVolume(connect, request);

            disks.add(OsRequest.Disk.builder()
                    .volume(request.getTargetVolume())
                    .deviceId(i)
                    .volumeType(volumeType)
                    .build());
        }


        List<OsRequest.Nic> nics = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            nics.add(OsRequest.Nic.builder()
                    .bridgeName("br0")
                    .mac(randomMacAddress())
                    .deviceId(i)
                    .driveType("virtio")
                    .build());
        }
        OsRequest.Start request = OsRequest.Start.builder()
                .emulator("/usr/bin/qemu-system-x86_64")
                .name("VM_TEST")
                .description("测试虚拟机")
                .bus(OsRequest.Disk.DiskBus.IDE)
                .cpu(OsRequest.Cpu.builder().number(2).core(1).socket(1).thread(1).share(500).build())
                .memory(OsRequest.Memory.builder().memory(1024 * 1024).build())
                .cdRoom(OsRequest.CdRoom.builder()
                        .path("/mnt/TEST_NFS/CentOS-7-x86_64-Minimal-2003.iso")
                        .build())
                .disks(disks)
                .networkInterfaces(nics)
                .vncPassword("123456")
                .build();
        new OsOperateImpl().start(connect, request);
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
