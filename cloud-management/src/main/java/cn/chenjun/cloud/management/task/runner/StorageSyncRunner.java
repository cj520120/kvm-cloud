package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.StorageCheckOperate;
import cn.chenjun.cloud.management.servcie.TaskService;
import cn.chenjun.cloud.management.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class StorageSyncRunner extends AbstractRunner {


    @Autowired
    private TaskService taskService;

    @Override
    public int getPeriodSeconds() {

        return configService.getConfig(Constant.ConfigKey.DEFAULT_CLUSTER_TASK_STORAGE_CHECK_TIMEOUT_SECOND);
    }

    @Override
    protected void dispatch() {
        BaseOperateParam operateParam = StorageCheckOperate.builder().id(UUID.randomUUID().toString()).title("检测存储池使用情况").build();
        this.taskService.addTask(operateParam);
    }

    @Override
    protected String getName() {
        return "存储池检测";
    }
}
