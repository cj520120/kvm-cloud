package cn.roamblue.cloud.agent.service.impl;

import cn.roamblue.cloud.agent.service.KvmStorageService;
import cn.roamblue.cloud.common.agent.StorageModel;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Error;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class KvmStorageServiceImpl extends AbstractKvmService implements KvmStorageService {

    @Override
    public List<StorageModel> listStorage() {

        return super.excute(connect -> {
            String[] pools = connect.listStoragePools();
            List<StorageModel> list = new ArrayList<>(pools.length);
            for (String pool : pools) {
                StorageModel storageInfo = this.getStorageInfo(pool);
                list.add(storageInfo);
            }
            return list;
        });
    }

    @Override
    public StorageModel getStorageInfo(String name) {
        return super.excute(connect -> {
            try {
                StoragePool storagePool = connect.storagePoolLookupByName(name);
                StoragePoolInfo storagePoolInfo = storagePool.getInfo();
                return StorageModel.builder().name(name)
                        .state(storagePoolInfo.state.toString())
                        .capacity(storagePoolInfo.capacity)
                        .allocation(storagePoolInfo.allocation)
                        .available(storagePoolInfo.available)
                        .build();
            } catch (LibvirtException err) {
                if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_STORAGE_POOL)) {
                    throw new CodeException(ErrorCode.AGENT_STORAGE_NOT_FOUND, "存储池不存在");
                } else {
                    throw err;
                }
            }
        });

    }

    @Override
    public void destroyStorage(String name) {
        super.excute(connect -> {
            try {
                StoragePool storagePool = connect.storagePoolLookupByName(name);
                storagePool.destroy();
                return null;
            } catch (LibvirtException err) {
                if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_STORAGE_POOL)) {
                    return null;
                } else {
                    throw err;
                }
            }
        });
    }

    private void createPath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    public StorageModel createStorage(String name, String nfs, String path, String target) {

        return super.excute(connect -> {
            String[] pools = connect.listStoragePools();
            boolean isExist = false;
            for (String pool : pools) {
                if (pool.equals(name)) {
                    isExist = true;
                    log.info("存储池已存在.name={} nfs={} path={} target={}", name, nfs, path, target);
                }
            }
            if (!isExist) {
                this.createPath(target);
                StringBuilder sb = new StringBuilder();
                sb.append("<pool type='netfs' xmlns:fs='http://libvirt.org/schemas/storagepool/fs/1.0'>")
                        .append("<name>").append(name).append("</name>")
                        .append("<source>")
                        .append("<host name='").append(nfs).append("'/>")
                        .append("<dir path='").append(path).append("'/>")
                        .append("</source>")
                        .append("<target>")
                        .append("<path>").append(target).append("</path>")
                        .append("</target>")
                        .append("</pool>");
                connect.storagePoolCreateXML(sb.toString(), 0);
                log.info("创建存储池.name={} nfs={} path={} target={}", name, nfs, path, target);
            }
            StoragePool storagePool = connect.storagePoolLookupByName(name);
            StoragePoolInfo storagePoolInfo = storagePool.getInfo();
            return StorageModel.builder().name(name)
                    .state(storagePoolInfo.state.toString())
                    .capacity(storagePoolInfo.capacity)
                    .allocation(storagePoolInfo.allocation)
                    .available(storagePoolInfo.available)
                    .build();
        });
    }
}
