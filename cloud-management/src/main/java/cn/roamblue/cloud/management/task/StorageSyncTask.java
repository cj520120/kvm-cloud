package cn.roamblue.cloud.management.task;

import cn.roamblue.cloud.management.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.operate.bean.BaseOperateParam;
import cn.roamblue.cloud.management.operate.bean.StorageCheckOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class StorageSyncTask extends AbstractTask {
    private final int TASK_CHECK_TIME = (int) TimeUnit.MINUTES.toSeconds(1);

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private StorageMapper storageMapper;
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
        if (rBucket.trySet(System.currentTimeMillis(), TASK_CHECK_TIME, TimeUnit.SECONDS)) {
            BaseOperateParam operateParam = StorageCheckOperate.builder().taskId(UUID.randomUUID().toString()).title("检测存储池使用情况").build();
            this.operateTask.addTask(operateParam);
        }
    }
}
