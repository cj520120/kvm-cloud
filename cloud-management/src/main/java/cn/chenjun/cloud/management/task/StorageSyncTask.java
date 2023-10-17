package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.StorageCheckOperate;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class StorageSyncTask extends AbstractTask {
    private final int TASK_CHECK_TIME = (int) TimeUnit.MINUTES.toSeconds(1);

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    @Lazy
    private OperateTask operateTask;

    @Override
    protected int getPeriodSeconds() {
        return TASK_CHECK_TIME;
    }

    @Override
    protected void dispatch() {
        RBucket<Long> rBucket = redissonClient.getBucket(RedisKeyUtil.STORAGE_SYNC_KEY);
        if (rBucket.isExists()) {
            return;
        }
        if (rBucket.setIfAbsent(System.currentTimeMillis(), Duration.ofSeconds(TASK_CHECK_TIME))) {
            BaseOperateParam operateParam = StorageCheckOperate.builder().taskId(UUID.randomUUID().toString()).title("检测存储池使用情况").build();
            this.operateTask.addTask(operateParam);
        }
    }
}
