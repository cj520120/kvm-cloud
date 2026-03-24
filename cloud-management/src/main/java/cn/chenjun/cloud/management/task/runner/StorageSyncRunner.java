package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.operate.bean.StorageCheckOperate;
import cn.chenjun.cloud.management.servcie.StorageService;
import cn.chenjun.cloud.management.servcie.TaskService;
import cn.chenjun.cloud.management.util.ConfigKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class StorageSyncRunner extends AbstractRunner {


    @Autowired
    private TaskService taskService;
    @Autowired
    private StorageService storageService;

    @Override
    public int getPeriodSeconds() {

        return configService.getConfig(ConfigKey.DEFAULT_TASK_STORAGE_CHECK_TIMEOUT_SECOND);
    }

    @Override
    protected void dispatch() {
        List<StorageEntity> storageList = this.storageService.listStorage();
        for (StorageEntity storage : storageList) {
            if (storage.getType() == Constant.StorageType.LOCAL && storage.getParentId() == 0) {
                continue;
            }
            BaseOperateParam operateParam = StorageCheckOperate.builder().id(UUID.randomUUID().toString()).storageId(storage.getStorageId()).title("检测存储池使用情况").build();
            this.taskService.addTask(operateParam);
        }
    }

    @Override
    public String getName() {
        return "存储池检测";
    }
}
