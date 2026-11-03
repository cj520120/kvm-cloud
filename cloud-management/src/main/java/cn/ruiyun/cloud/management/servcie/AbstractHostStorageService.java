package cn.ruiyun.cloud.management.servcie;

import cn.ruiyun.cloud.common.core.operate.BaseOperateParam;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.data.entity.HostEntity;
import cn.ruiyun.cloud.management.data.entity.StorageEntity;
import cn.ruiyun.cloud.management.operate.bean.CreateStorageOperate;
import cn.ruiyun.cloud.management.util.NotifyContextHolderUtil;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public abstract class AbstractHostStorageService extends AbstractService {
    @Transactional(rollbackFor = Exception.class)
    public void checkAndInitHostLocalStorage(StorageEntity localStorage, HostEntity host) {
        String path = localStorage.getMountPath();
        StorageEntity hostStorage = this.storageDao.findByLocalStorage(localStorage.getStorageId(), host.getHostId());
        if (hostStorage == null) {
            String storageName = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            StorageEntity storage = StorageEntity.builder()
                    .description(host.getDisplayName())
                    .name(storageName)
                    .type(Constant.StorageType.LOCAL)
                    .hostId(host.getHostId())
                    .param(localStorage.getParam())
                    .mountPath(path)
                    .supportCategory(Constant.StorageCategory.VOLUME)
                    .allocation(0L)
                    .capacity(0L)
                    .available(0L)
                    .parentId(localStorage.getStorageId())
                    .status(Constant.StorageStatus.INIT)
                    .build();
            this.storageDao.insert(storage);
            BaseOperateParam operateParam = CreateStorageOperate.builder().id(UUID.randomUUID().toString()).title("创建存储池[" + storage.getName() + "]").storageId(storage.getStorageId()).build();
            this.operateTask.addTask(operateParam);
            NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(storage.getStorageId()).type(Constant.NotifyType.UPDATE_STORAGE).build());
        }
    }
}
