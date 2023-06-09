package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.mapper.StorageMapper;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.VolumeCheckOperate;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class VolumeSyncTask extends AbstractTask {

    private final int TASK_CHECK_TIME = (int) TimeUnit.MINUTES.toSeconds(10);
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

        RBucket<Long> rBucket = redissonClient.getBucket(RedisKeyUtil.VOLUME_SYNC_KEY);
        if (rBucket.isExists()) {
            return;
        }
        if (rBucket.setIfAbsent(System.currentTimeMillis(), Duration.ofSeconds(TASK_CHECK_TIME))) {
            List<StorageEntity> storageList = this.storageMapper.selectList(new QueryWrapper<>()).stream().filter(t -> Objects.equals(t.getStatus(), Constant.StorageStatus.READY)).collect(Collectors.toList());
            for (StorageEntity storage : storageList) {
                BaseOperateParam operateParam = VolumeCheckOperate.builder().taskId(UUID.randomUUID().toString()).title("检测存储池磁盘使用情况").storageId(storage.getStorageId()).build();
                this.operateTask.addTask(operateParam);

            }
        }
    }

}
