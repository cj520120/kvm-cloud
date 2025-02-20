package cn.chenjun.cloud.agent.util;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import org.libvirt.Connect;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo;

/**
 * @author chenjun
 */
public class StorageUtil {
    private final static Object SYNC_OBJ = new Object();

    public static StoragePool findStorage(Connect connect, String name) {
        synchronized (SYNC_OBJ) {
            while (true) {
                try {
                    StoragePool storagePool = connect.storagePoolLookupByName(name);
                    if (storagePool != null) {
                        StoragePoolInfo storagePoolInfo = storagePool.getInfo();
                        if (storagePoolInfo.state != StoragePoolInfo.StoragePoolState.VIR_STORAGE_POOL_RUNNING) {
                            storagePool.destroy();
                            return null;
                        }
                        try {
                            storagePool.refresh(0);
                        } catch (Exception ignored) {
                            continue;
                        }
                    }
                    return storagePool;
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
    }

    public static void checkStorageSuccess(Connect connect, String name) {
        StoragePool pool = findStorage(connect, name);
        if (pool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_READY, "当前存储池未就绪:[" + name + "]");
        }
    }
}
