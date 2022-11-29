package cn.roamblue.cloud.agent.operate.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.roamblue.cloud.agent.operate.StorageOperate;
import cn.roamblue.cloud.common.agent.StorageModel;
import cn.roamblue.cloud.common.agent.StorageRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.common.util.StorageType;
import org.libvirt.Connect;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo;

import java.util.Objects;

/**
 * @author chenjun
 */
public class StorageOperateImpl implements StorageOperate {
    @Override
    public StorageModel create(Connect connect, StorageRequest request) throws Exception {
        if (!Objects.equals(StorageType.NFS, request.getType())) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的存储池类型:" + request.getType());
        }
        StoragePool storagePool = this.findStorage(connect, request.getName());
        if (storagePool == null) {
            String nfsUri = request.getParam().get("uri").toString();
            String nfsPath = request.getParam().get("path").toString();
            String mountPath = request.getParam().get("mount").toString();
            String xml = ResourceUtil.readUtf8Str("xml/storage/NfsStorage.xml");
            xml = String.format(xml, request.getName(), nfsUri, nfsPath, mountPath);
            storagePool = connect.storagePoolCreateXML(xml, 0);
        }
        StoragePoolInfo storagePoolInfo = storagePool.getInfo();
        if (storagePoolInfo.state != StoragePoolInfo.StoragePoolState.VIR_STORAGE_POOL_RUNNING) {
            storagePool.setAutostart(1);
            storagePool.create(1);
        }
        storagePool.refresh(1);
        return StorageModel.builder().name(request.getName())
                .state(storagePoolInfo.state.toString())
                .capacity(storagePoolInfo.capacity)
                .allocation(storagePoolInfo.allocation)
                .available(storagePoolInfo.available)
                .build();
    }

    @Override
    public void destroy(Connect connect, String name) throws Exception {

        StoragePool storagePool = this.findStorage(connect, name);
        if (storagePool != null) {
            storagePool.destroy();
            storagePool.undefine();
        }
    }

    private StoragePool findStorage(Connect connect, String name) throws Exception {
        String[] pools = connect.listStoragePools();
        boolean isExist = false;
        for (String pool : pools) {
            if (Objects.equals(pool, name)) {
                isExist = true;
                break;
            }
        }
        if (isExist) {
            return connect.storagePoolLookupByName(name);
        } else {
            return null;
        }
    }
}
