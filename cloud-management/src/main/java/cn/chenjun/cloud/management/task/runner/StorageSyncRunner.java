package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.mapper.StorageMapper;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.StorageCheckOperate;
import cn.chenjun.cloud.management.servcie.TaskService;
import cn.chenjun.cloud.management.util.ConfigKey;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    private StorageMapper storageMapper;

    @Override
    public int getPeriodSeconds() {

        return configService.getConfig(ConfigKey.DEFAULT_TASK_STORAGE_CHECK_TIMEOUT_SECOND);
    }

    @Override
    protected void dispatch() {
        List<StorageEntity> storageList = this.storageMapper.selectList(new QueryWrapper<>());
        for (StorageEntity storage : storageList) {
            BaseOperateParam operateParam = StorageCheckOperate.builder().id(UUID.randomUUID().toString()).storageId(storage.getStorageId()).title("检测存储池使用情况").build();
            this.taskService.addTask(operateParam);
        }
    }

    @Override
    protected String getName() {
        return "存储池检测";
    }
}
