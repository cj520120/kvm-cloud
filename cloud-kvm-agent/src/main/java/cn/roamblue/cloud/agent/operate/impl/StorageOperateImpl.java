package cn.roamblue.cloud.agent.operate.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.roamblue.cloud.agent.operate.StorageOperate;
import cn.roamblue.cloud.common.bean.StorageCreateRequest;
import cn.roamblue.cloud.common.bean.StorageDestroyRequest;
import cn.roamblue.cloud.common.bean.StorageInfo;
import cn.roamblue.cloud.common.bean.StorageInfoRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import org.libvirt.Connect;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author chenjun
 */
@Component
public class StorageOperateImpl implements StorageOperate {
    @Override
    public StorageInfo getStorageInfo(Connect connect, StorageInfoRequest request) throws Exception {
        StoragePool storagePool = this.findStorage(connect, request.getName());
        if (storagePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在:" + request.getName());
        }
        storagePool.refresh(0);
        StoragePoolInfo storagePoolInfo = storagePool.getInfo();
        return StorageInfo.builder().name(request.getName())
                .state(storagePoolInfo.state.toString())
                .capacity(storagePoolInfo.capacity)
                .allocation(storagePoolInfo.allocation)
                .available(storagePoolInfo.available)
                .build();
    }

    @Override
    public List<StorageInfo> batchStorageInfo(Connect connect, List<StorageInfoRequest> batchRequest) throws Exception {
        List<StorageInfo> list = new ArrayList<>();
        for (StorageInfoRequest request : batchRequest) {
            StoragePool storagePool = this.findStorage(connect, request.getName());
            StorageInfo model = null;
            if (storagePool != null) {
                storagePool.refresh(0);
                StoragePoolInfo storagePoolInfo = storagePool.getInfo();
                model = StorageInfo.builder().name(request.getName())
                        .state(storagePoolInfo.state.toString())
                        .capacity(storagePoolInfo.capacity)
                        .allocation(storagePoolInfo.allocation)
                        .available(storagePoolInfo.available)
                        .build();
            }
            list.add(model);

        }
        return list;
    }

    @Override
    public StorageInfo create(Connect connect, StorageCreateRequest request) throws Exception {
        if (!Objects.equals(Constant.StorageType.NFS, request.getType())) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的存储池类型:" + request.getType());
        }
        StoragePool storagePool = this.findStorage(connect, request.getName());
        if (storagePool != null) {
            StoragePoolInfo storagePoolInfo = storagePool.getInfo();
            if (storagePoolInfo.state != StoragePoolInfo.StoragePoolState.VIR_STORAGE_POOL_RUNNING) {
                storagePool.destroy();
                storagePool = null;
            }
        }
        if (storagePool == null) {
            String nfsUri = request.getParam().get("uri").toString();
            String nfsPath = request.getParam().get("path").toString();
            FileUtil.mkdir(request.getMountPath());
            String xml = ResourceUtil.readUtf8Str("xml/storage/NfsStorage.xml");
            xml = String.format(xml, request.getName(), nfsUri, nfsPath, request.getMountPath());
            storagePool = connect.storagePoolCreateXML(xml, 0);
        }
        storagePool.refresh(0);
        StoragePoolInfo storagePoolInfo = storagePool.getInfo();
        return StorageInfo.builder().name(request.getName())
                .state(storagePoolInfo.state.toString())
                .capacity(storagePoolInfo.capacity)
                .allocation(storagePoolInfo.allocation)
                .available(storagePoolInfo.available)
                .build();
    }

    @Override
    public void destroy(Connect connect, StorageDestroyRequest request) throws Exception {

        StoragePool storagePool = this.findStorage(connect, request.getName());
        if (storagePool != null) {
            storagePool.destroy();
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
