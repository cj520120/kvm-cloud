package cn.chenjun.cloud.common.util;

/**
 * @author chenjun
 */
public class Constant {
    public static class VolumeType {
        public static final String RAW = "raw";
        public static final String QCOW = "qcow";
        public static final String QCOW2 = "qcow2";
        public static final String VDI = "vdi";
        public static final String VMDK = "vmdk";
        public static final String VPC = "vpc";

    }

    public static class Command {
        public static final String CHECK_TASK = "CheckTask";
        public static final String SUBMIT_TASK = "SubmitTask";
        public static final String HOST_INFO = "HostInfo";
        public static final String HOST_INIT = "HostInit";
        public static final String STORAGE_INFO = "StorageInfo";
        public static final String BATCH_STORAGE_INFO = "BatchStorageInfo";
        public static final String STORAGE_CREATE = "CreateStorage";
        public static final String STORAGE_DESTROY = "DestroyStorage";

        public static final String NETWORK_CREATE_BASIC = "CreateBasicNetwork";
        public static final String NETWORK_DESTROY_BASIC = "DestroyBasicNetwork";
        public static final String NETWORK_CREATE_VLAN = "CreateVlanNetwork";
        public static final String NETWORK_DESTROY_VLAN = "DestroyVlanNetwork";
        public static final String VOLUME_INFO = "VolumeInfo";
        public static final String BATCH_VOLUME_INFO = "BatchVolumeInfo";
        public static final String VOLUME_CREATE = "CreateVolume";
        public static final String VOLUME_DESTROY = "DestroyVolume";
        public static final String VOLUME_CLONE = "CloneVolume";
        public static final String VOLUME_RESIZE = "ResizeVolume";

        public static final String VOLUME_MIGRATE = "MigrateVolume";
        public static final String VOLUME_SNAPSHOT = "SnapshotVolume";

        public static final String VOLUME_TEMPLATE = "TemplateVolume";
        public static final String VOLUME_DOWNLOAD = "DownloadVolume";

        public static final String GUEST_DESTROY = "DeleteGuest";
        public static final String GUEST_START = "StartGuest";
        public static final String GUEST_INFO = "GuestInfo";
        public static final String BATCH_GUEST_INFO = "BatchGuestInfo";
        public static final String ALL_GUEST_INFO = "AllGuestInfo";
        public static final String GUEST_REBOOT = "RebootGuest";
        public static final String GUEST_SHUTDOWN = "ShutdownGuest";
        public static final String GUEST_MIGRATE = "MigrateGuest";

        public static final String GUEST_ATTACH_CD_ROOM = "AttachCdRoom";
        public static final String GUEST_DETACH_CD_ROOM = "DetachCdRoom";
        public static final String GUEST_ATTACH_DISK = "AttachDisk";
        public static final String GUEST_DETACH_DISK = "DetachDisk";
        public static final String GUEST_ATTACH_NIC = "AttachNic";
        public static final String GUEST_DETACH_NIC = "DetachNic";
        public static final String GUEST_QMA = "QMAGuest";

    }

    /**
     * @ClassName: StorageType
     * @Description: TODO
     * @Create by: chenjun
     * @Date: 2021/8/5 上午11:12
     */
    public static class StorageType {
        public static final String NFS = "nfs";
        public static final String ISCSI = "iscsi";
        public static final String LOCAL = "local";
    }

    public static class DiskBus {
        public static final String VIRTIO = "virtio";
        public static final String IDE = "ide";
        public static final String SCSI = "scsi";

    }

    public static class NetworkDriver {
        public static final String VIRTIO = "virtio";
        public static final String RTL8139 = "rtl8139";
        public static final String E1000 = "e1000";

    }

    public static class NotifyType {
        public static final int UPDATE_GUEST = 1;
        public static final int UPDATE_VOLUME = 2;
        public static final int UPDATE_NETWORK = 3;
        public static final int UPDATE_HOST = 4;
        public static final int UPDATE_TEMPLATE = 5;
        public static final int UPDATE_SNAPSHOT = 6;
        public static final int UPDATE_STORAGE = 7;
        public static final int UPDATE_SCHEME = 8;
    }
}
