package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.annotation.DispatchBind;
import cn.chenjun.cloud.agent.util.StorageUtil;
import cn.chenjun.cloud.common.bean.StorageCreateRequest;
import cn.chenjun.cloud.common.bean.StorageDestroyRequest;
import cn.chenjun.cloud.common.bean.StorageInfo;
import cn.chenjun.cloud.common.bean.StorageInfoRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.codec.Base64;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.Secret;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class StorageOperate {


    @DispatchBind(command = Constant.Command.STORAGE_INFO)

    public StorageInfo getStorageInfo(Connect connect, StorageInfoRequest request) throws Exception {
        StoragePool storagePool = StorageUtil.findStorage(connect, request.getName(), false);
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

    @DispatchBind(command = Constant.Command.BATCH_STORAGE_INFO, async = true)

    public List<StorageInfo> batchStorageInfo(Connect connect, List<StorageInfoRequest> batchRequest) throws Exception {
        List<StorageInfo> list = new ArrayList<>();
        for (StorageInfoRequest request : batchRequest) {
            StoragePool storagePool = StorageUtil.findStorage(connect, request.getName(), false);
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

    @DispatchBind(command = Constant.Command.STORAGE_CREATE, async = true)

    public StorageInfo create(Connect connect, StorageCreateRequest request) throws Exception {
        if (!ObjectUtils.isEmpty(request.getSecretXml())) {
            Secret secret;
            try {
                secret = connect.secretLookupByUUIDString(request.getName());
            } catch (Exception err) {
                log.info("创建 secret:{} xml={}", request.getName(), request.getSecretXml());
                secret = connect.secretDefineXML(request.getSecretXml());
            }
            secret.setValue(Base64.decode(request.getSecretValue()));
        }
        synchronized (request.getName().intern()) {
            StoragePool storagePool = StorageUtil.findStorage(connect, request.getName(), true);
            if (storagePool == null) {
                log.info("创建 storage:{} xml={}", request.getName(), request.getStorageXml());
                storagePool = connect.storagePoolDefineXML(request.getStorageXml(), 0);
                storagePool.setAutostart(1);
            }
            if (storagePool.isActive() == 0) {
                storagePool.create(0);
            }
            StoragePoolInfo storagePoolInfo = storagePool.getInfo();
            return StorageInfo.builder().name(request.getName())
                    .state(storagePoolInfo.state.toString())
                    .capacity(storagePoolInfo.capacity)
                    .allocation(storagePoolInfo.allocation)
                    .available(storagePoolInfo.available)
                    .build();
        }
    }

    @DispatchBind(command = Constant.Command.STORAGE_DESTROY, async = true)

    public Void destroy(Connect connect, StorageDestroyRequest request) throws Exception {
        synchronized (request.getName().intern()) {
            StoragePool storagePool = StorageUtil.findStorage(connect, request.getName(), true);
            if (storagePool != null) {
                storagePool.destroy();
                storagePool.undefine();
            }
            if (Objects.equals(request.getType(), Constant.StorageType.CEPH_RBD)) {
                try {
                    connect.secretLookupByUUIDString(request.getName()).undefine();
                } catch (Exception err) {

                }
            }
            return null;
        }

    }
}
