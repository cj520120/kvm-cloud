package cn.roamblue.cloud.management.task;

import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.operate.bean.BaseOperateParam;
import cn.roamblue.cloud.management.operate.bean.HostCheckOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class HostSyncTask extends AbstractTask {
    private final int TASK_CHECK_TIME = 30;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    @Lazy
    private OperateTask operateTask;


    @Override
    protected void dispatch() {
        RBucket<Long> rBucket = redissonClient.getBucket(RedisKeyUtil.HOST_SYNC_KEY);
        if (rBucket.isExists()) {
            return;
        }
        if (rBucket.trySet(System.currentTimeMillis(), TASK_CHECK_TIME, TimeUnit.SECONDS)) {
            List<HostEntity> hostList = hostMapper.selectList(new QueryWrapper<>());
            for (HostEntity host : hostList) {
                switch (host.getStatus()) {
                    case cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE:
                    case cn.roamblue.cloud.management.util.Constant.HostStatus.OFFLINE:
                        BaseOperateParam operateParam = HostCheckOperate.builder().taskId(UUID.randomUUID().toString()).title("检测主机状态").hostId(host.getHostId()).build();
                        this.operateTask.addTask(operateParam);
                        break;
                }
            }
        }
    }
}
