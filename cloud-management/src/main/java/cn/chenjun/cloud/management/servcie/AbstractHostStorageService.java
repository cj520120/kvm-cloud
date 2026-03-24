package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.operate.bean.CreateStorageOperate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
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
            this.notifyService.publish(NotifyData.<Void>builder().id(storage.getStorageId()).type(Constant.NotifyType.UPDATE_STORAGE).build());
        }
    }
}
