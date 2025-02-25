package cn.chenjun.cloud.agent.util;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.StoragePool;

/**
 * @author chenjun
 */
@Slf4j
public class StorageUtil {
    private final static Object SYNC_OBJ = new Object();

    public static StoragePool findStorage(Connect connect, String name, boolean autoDestroy) {
        synchronized (SYNC_OBJ) {
            while (true) {
                try {
                    StoragePool storagePool = connect.storagePoolLookupByName(name);
                    if (storagePool != null) {
                        if (storagePool.isActive() == 0) {
                            if (autoDestroy) {
                                log.info("storage[{}] not running.destroy ", name);
                                storagePool.undefine();
                                return null;
                            } else {
                                storagePool.create(0);
                            }
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
        StoragePool pool = findStorage(connect, name, false);
        if (pool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_READY, "当前存储池未就绪:[" + name + "]");
        }
    }
}
