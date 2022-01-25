package cn.roamblue.cloud.agent.service.impl;

import cn.roamblue.cloud.agent.service.KvmStorageService;
import cn.roamblue.cloud.agent.service.impl.storage.impl.StroageInitializeFactory;
import cn.roamblue.cloud.common.agent.StorageModel;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Error;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private StroageInitializeFactory stroageBuilderStrategy;

    @Override
    public List<StorageModel> listStorage() {

        return super.execute(connect -> {
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
        return super.execute(connect -> {
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
                    throw new CodeException(ErrorCode.AGENT_STORAGE_NOT_FOUND, "storage not found");
                } else {
                    throw err;
                }
            }
        });

    }

    @Override
    public void destroyStorage(String name) {
        super.execute(connect -> {
        	while(true) {
	            try {
	                StoragePool storagePool = connect.storagePoolLookupByName(name);
	                if(storagePool==null) {
	                	break;
	                }
	                storagePool.destroy();
	                storagePool.undefine();
	                break;
	            } catch (LibvirtException err) {
	                if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_STORAGE_POOL)) {
	                    break;
	                } else {
	                    throw err;
	                }
	            }
        	}
            return null;
        });
    }

    private void createPath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    public StorageModel createStorage(String type,String name, String uri, String path, String target) {

        return super.execute(connect -> {
            String[] pools = connect.listStoragePools();
            boolean isExist = false;
            for (String pool : pools) {
                if (pool.equals(name)) {
                    isExist = true;
                    log.info("storage exists.name={} uri={} path={} target={}", name, uri, path, target);
                }
            }
            if (!isExist) {
                this.createPath(target);
                stroageBuilderStrategy.find(type).initialize(connect,name, uri, path, target);
                log.info("create storage.name={} uri={} path={} target={}", name, uri, path, target);
            }
            StoragePool storagePool = connect.storagePoolLookupByName(name); 
            StoragePoolInfo storagePoolInfo = storagePool.getInfo();
            if(storagePoolInfo.state!=StoragePoolInfo.StoragePoolState.VIR_STORAGE_POOL_RUNNING) { 
            	storagePool.setAutostart(1);
            	storagePool.create(1);
            }
            return StorageModel.builder().name(name)
                    .state(storagePoolInfo.state.toString())
                    .capacity(storagePoolInfo.capacity)
                    .allocation(storagePoolInfo.allocation)
                    .available(storagePoolInfo.available)
                    .build();
        });
    }
}
