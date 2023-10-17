package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.HostCheckOperate;
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
import java.util.UUID;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class HostSyncTask extends AbstractTask {
    private static final int TASK_CHECK_TIME = 30;
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
        if (rBucket.setIfAbsent(System.currentTimeMillis(), Duration.ofSeconds(TASK_CHECK_TIME))) {
            List<HostEntity> hostList = hostMapper.selectList(new QueryWrapper<>());
            for (HostEntity host : hostList) {
                switch (host.getStatus()) {
                    case Constant.HostStatus.ONLINE:
                    case Constant.HostStatus.OFFLINE:
                        BaseOperateParam operateParam = HostCheckOperate.builder().taskId(UUID.randomUUID().toString()).title("检测主机状态").hostId(host.getHostId()).build();
                        this.operateTask.addTask(operateParam);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
