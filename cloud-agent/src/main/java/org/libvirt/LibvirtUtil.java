package org.libvirt;

import org.libvirt.jna.Libvirt;
import org.libvirt.jna.virStorageVolInfo;

/**
 * @author chenjun
 */
public class LibvirtUtil extends ErrorHandler {
    public static StorageVolInfo getVolInfo(StorageVol vol) throws Exception {
        virStorageVolInfo vInfo = new virStorageVolInfo();
        processError(Libvirt.INSTANCE.virStorageVolGetInfo(vol.vsvp, vInfo));
        return new StorageVolInfo(vInfo);
    }
    public static class StorageVolInfo {

        public enum VirStorageVolType {
            /**
             * Regular file based volumes
             */
            VIR_STORAGE_VOL_FILE,
            /**
             * Block based volumes
             */
            VIR_STORAGE_VOL_BLOCK,
            /**
             * Block based volumes
             */
            VIR_STORAGE_VOL_NETWORK
        }

        /**
         * The type of the Volume
         */
        public VirStorageVolType type;
        /**
         * Logical size bytes
         */
        public long capacity;

        /**
         * Current allocation bytes
         */
        public long allocation;;

        /**
         * This is meant to be called from the JNI side, as a convenience
         * constructor
         *
         * @param type
         *            the type, as defined by libvirt
         * @param capacity
         * @param allocation
         */
        StorageVolInfo(final int type, final long capacity, final long allocation) {
            switch (type) {
                case 0:
                    this.type = VirStorageVolType.VIR_STORAGE_VOL_FILE;
                    break;
                case 1:
                    this.type =VirStorageVolType.VIR_STORAGE_VOL_BLOCK;
                    break;
                case 3:
                    this.type =VirStorageVolType.VIR_STORAGE_VOL_NETWORK;
                    break;
                default:
                    assert false;
            }
            this.capacity = capacity;
            this.allocation = allocation;
        }

        StorageVolInfo(final virStorageVolInfo volInfo) {
            this(volInfo.type, volInfo.capacity, volInfo.allocation);
        }

        @Override
        public String toString() {
            return String.format("type:%s%ncapacity:%d%nallocation:%d%n", type, capacity, allocation);
        }
    }
}
