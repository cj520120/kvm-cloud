package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
public abstract class AbstractRunner {

    @Autowired
    protected RedissonClient redissonClient;
    @Autowired
    protected ConfigService configService;

    @Autowired
    private LockRunner lockRunner;

    public void call() {
        try {
            if (!this.isStart()) {
                return;
            }
            lockRunner.lockRun(RedisKeyUtil.GLOBAL_LOCK_KEY, () -> {
                if (this.getPeriodSeconds() > 0) {
                    String key = RedisKeyUtil.JOB_RUN_TIME + this.getClass().getName();
                    RBucket<Long> rBucket = redissonClient.getBucket(key);
                    if (rBucket.isExists()) {
                        return;
                    }
                    rBucket.set(System.currentTimeMillis(), Math.max(1, this.getPeriodSeconds()), TimeUnit.SECONDS);
                }
                try {
                    log.info("开始执行周期任务: {}，当前任务周期 {}s", this.getName(), this.getPeriodSeconds());
                    this.dispatch();
                } catch (Exception err) {
                    log.info("执行周期任务失败: {}", this.getName(), err);
                }
            });
        } catch (Exception err) {
            log.error("周期任务执行失败.", err);
        }
    }

    public int getPeriodSeconds() {
        return 10;
    }

    public int getDelaySeconds() {
        return 10;
    }

    /**
     * 任务分发
     *
     * @throws Exception
     */
    protected abstract void dispatch() throws Exception;

    /**
     * 任务名称
     *
     * @return
     */
    protected abstract String getName();

    protected boolean isStart() {
        return true;
    }
}
