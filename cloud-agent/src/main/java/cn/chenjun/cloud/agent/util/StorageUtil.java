package cn.chenjun.cloud.agent.util;

import org.libvirt.Connect;
import org.libvirt.StoragePool;

/**
 * @author chenjun
 */
public class StorageUtil {
    private final static Object SYNC_OBJ = new Object();

    public static StoragePool findStorage(Connect connect, String name) {
        synchronized (SYNC_OBJ) {
            try {
                StoragePool storagePool = connect.storagePoolLookupByName(name);
                synchronized (name.intern()) {
                    try {
                        storagePool.refresh(0);
                    } catch (Exception ignored) {

                    }
                }
                return storagePool;
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
