package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.StorageOperate;
import cn.chenjun.cloud.agent.operate.annotation.DispatchBind;
import cn.chenjun.cloud.agent.util.StorageUtil;
import cn.chenjun.cloud.agent.util.TemplateUtil;
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
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class StorageOperateImpl implements StorageOperate {


    @DispatchBind(command = Constant.Command.STORAGE_INFO)
    @Override
    public StorageInfo getStorageInfo(Connect connect, StorageInfoRequest request) throws Exception {
        StoragePool storagePool = StorageUtil.findStorage(connect, request.getName());
        if (storagePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在:" + request.getName());
        }
        StoragePoolInfo storagePoolInfo = storagePool.getInfo();
        return StorageInfo.builder().name(request.getName())
                .state(storagePoolInfo.state.toString())
                .capacity(storagePoolInfo.capacity)
                .allocation(storagePoolInfo.allocation)
                .available(storagePoolInfo.available)
                .build();
    }

    @DispatchBind(command = Constant.Command.BATCH_STORAGE_INFO)
    @Override
    public List<StorageInfo> batchStorageInfo(Connect connect, List<StorageInfoRequest> batchRequest) throws Exception {
        List<StorageInfo> list = new ArrayList<>();
        for (StorageInfoRequest request : batchRequest) {
            StoragePool storagePool = StorageUtil.findStorage(connect, request.getName());
            StorageInfo model = null;
            if (storagePool != null) {
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

    @DispatchBind(command = Constant.Command.STORAGE_CREATE)
    @Override
    public StorageInfo create(Connect connect, StorageCreateRequest request) throws Exception {
        synchronized (request.getName().intern()) {
            StoragePool storagePool = StorageUtil.findStorage(connect, request.getName());
            if (storagePool != null) {
                StoragePoolInfo storagePoolInfo = storagePool.getInfo();
                if (storagePoolInfo.state != StoragePoolInfo.StoragePoolState.VIR_STORAGE_POOL_RUNNING) {
                    storagePool.destroy();
                    storagePool = null;
                }
            }

            if (storagePool == null) {
                switch (request.getType()) {
                    case Constant.StorageType.NFS: {
                        String nfsUri = request.getParam().get("uri").toString();
                        String nfsPath = request.getParam().get("path").toString();
                        FileUtil.mkdir(request.getMountPath());
                        String xml = ResourceUtil.readUtf8Str("tpl/nfs_storage.xml");

                        Map<String, Object> map = new HashMap<>(5);
                        map.put("name", request.getName());
                        map.put("uri", nfsUri);
                        map.put("path", nfsPath);
                        map.put("mount", request.getMountPath());

                        Jinjava jinjava = TemplateUtil.create();
                        xml = jinjava.render(xml, map);
                        log.info("createStorage xml={}", xml);
                        storagePool = connect.storagePoolCreateXML(xml, 0);
                    }
                    break;
                    case Constant.StorageType.GLUSTERFS: {
                        String glusterUri = request.getParam().get("uri").toString();
                        String volume = request.getParam().get("path").toString();
                        FileUtil.mkdir(request.getMountPath());
                        String xml = ResourceUtil.readUtf8Str("tpl/glusterfs_storage.xml");
                        Map<String, Object> map = new HashMap<>(4);
                        map.put("name", request.getName());
                        map.put("host", glusterUri);
                        map.put("volume", volume);
                        map.put("mount", request.getMountPath());
                        Jinjava jinjava = TemplateUtil.create();
                        xml = jinjava.render(xml, map);
                        log.info("createStorage xml={}", xml);
                        storagePool = connect.storagePoolCreateXML(xml, 0);
                    }
                    break;
                    default:
                        throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的存储池类型:" + request.getType());
                }
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
    }

    @DispatchBind(command = Constant.Command.STORAGE_DESTROY)
    @Override
    public Void destroy(Connect connect, StorageDestroyRequest request) throws Exception {
        synchronized (request.getName().intern()) {
            StoragePool storagePool = StorageUtil.findStorage(connect, request.getName());
            if (storagePool != null) {
                storagePool.destroy();
            }
            return null;
        }
    }


}
