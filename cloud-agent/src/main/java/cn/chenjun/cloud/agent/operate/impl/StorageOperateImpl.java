package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.StorageOperate;
import cn.chenjun.cloud.common.bean.StorageCreateRequest;
import cn.chenjun.cloud.common.bean.StorageDestroyRequest;
import cn.chenjun.cloud.common.bean.StorageInfo;
import cn.chenjun.cloud.common.bean.StorageInfoRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.hubspot.jinjava.Jinjava;
import org.libvirt.Connect;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo;
import org.springframework.stereotype.Component;

import java.util.*;

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
        switch (request.getType()) {
            case Constant.StorageType.NFS:
            case Constant.StorageType.GLUSTERFS:
                break;
            default:
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
            String xml = ResourceUtil.readUtf8Str("tpl/storage.xml");
            Map<String, Object> map = new HashMap<>(0);
            map.put("name", request.getName());
            map.put("host", nfsUri);
            map.put("path", nfsPath);
            map.put("mount", request.getMountPath());
            if (request.getType().equals(Constant.StorageType.GLUSTERFS)) {
                map.put("format", "glusterfs");
            } else {
                map.put("format", "auto");
            }
            Jinjava jinjava = new Jinjava();
            xml = jinjava.render(xml, map);
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
