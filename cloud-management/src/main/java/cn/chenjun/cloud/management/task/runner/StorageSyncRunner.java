package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.StorageCheckOperate;
import cn.chenjun.cloud.management.servcie.TaskService;
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
        return 60;
    }

    @Override
    protected void dispatch() {
        BaseOperateParam operateParam = StorageCheckOperate.builder().taskId(UUID.randomUUID().toString()).title("检测存储池使用情况").build();
        this.taskService.addTask(operateParam);
    }

    @Override
    protected String getName() {
        return "存储池检测";
    }

    @Override
    protected boolean canRunning() {
        return !this.taskService.hasTask(StorageCheckOperate.class);
    }
}
