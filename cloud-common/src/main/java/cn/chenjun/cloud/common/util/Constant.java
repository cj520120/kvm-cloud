package cn.chenjun.cloud.common.util;

import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * @author chenjun
 */
public class Constant {
    public enum NetworkBridgeType {

        /**
         * 基础网络
         */
        BASIC(0, "bridge"),
        OPEN_SWITCH(1, "OpenSwitch");
        private final String bridgeName;
        private final int bridgeType;

        NetworkBridgeType(int bridgeType, String bridgeName) {
            this.bridgeType = bridgeType;
            this.bridgeName = bridgeName;
        }

        public static NetworkBridgeType fromBridgeValue(String value) {
            return Arrays.stream(NetworkBridgeType.values()).filter(br -> StringUtils.endsWithIgnoreCase(br.bridgeName, value)).findFirst().orElse(null);
        }

        public static NetworkBridgeType fromBridgeType(int type) {
            return Arrays.stream(NetworkBridgeType.values()).filter(br -> br.bridgeType == type).findFirst().orElse(null);
        }

        public int bridgeType() {
            return this.bridgeType;
        }

        public String bridgeName() {
            return this.bridgeName;
        }
    }

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
        public static final String DESTROY_UNLINK_VOLUME = "DestroyUnLinkVolume";
        public static final String VOLUME_CLONE = "CloneVolume";
        public static final String VOLUME_RESIZE = "ResizeVolume";
        public static final String LIST_STORAGE_VOLUME = "ListStorageVolume";

        public static final String VOLUME_MIGRATE = "MigrateVolume";
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


    public static class StorageType {
        public static final String NFS = "nfs";
        public static final String GLUSTERFS = "glusterfs";
        public static final String CEPH_RBD = "ceph-rbd";
        public static final String LOCAL = "local";
    }

    public static class DiskDriveType {
        public static final String VIRTIO = "virtio";
        public static final String IDE = "ide";
        public static final String SCSI = "scsi";
        public static final String SATA = "sata";

    }

    public static class NetworkDriver {
        public static final String VIRTIO = "virtio";
        public static final String RTL8139 = "rtl8139";
        public static final String E1000 = "e1000";

    }

    public static class SocketCommand {


        public static final int WEB_LOGIN = 100;
        public static final int WEB_LOGIN_SUCCESS = 101;
        public static final int WEB_LOGIN_TOKEN_ERROR = 102;
        public static final int WEB_NOTIFY = 103;


        public static final int COMPONENT_CONNECT = 200;
        public static final int COMPONENT_CONNECT_SUCCESS = 201;
        public static final int COMPONENT_CONNECT_FAIL = 202;
        public static final int COMPONENT_NOTIFY = 203;
        public static final int COMPONENT_DNS_REQUEST = 206;
        public static final int COMPONENT_NAT_REQUEST = 207;


    }

    public static class NotifyType {

        public static final int UPDATE_GUEST = 1;

        public static final int UPDATE_VOLUME = 2;
        public static final int UPDATE_NETWORK = 3;
        public static final int UPDATE_HOST = 4;
        public static final int UPDATE_TEMPLATE = 5;
        public static final int UPDATE_STORAGE = 7;
        public static final int UPDATE_SCHEME = 8;
        public static final int UPDATE_GROUP = 9;
        public static final int UPDATE_DNS = 10;
        public static final int UPDATE_COMPONENT = 11;
        public static final int UPDATE_COMPONENT_NAT = 12;
        public static final int UPDATE_SSH = 13;

        public static final int UPDATE_USER = 14;

        public static final int COMPONENT_UPDATE_DNS = 101;
        public static final int COMPONENT_UPDATE_NAT = 102;



        public static final int GUEST_START_CALLBACK_NOTIFY = 301;
        public static final int GUEST_STOP_CALLBACK_NOTIFY = 302;
        public static final int GUEST_RESTART_CALLBACK_NOTIFY = 303;
    }

}
