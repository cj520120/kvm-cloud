package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.util.RequestContextHolderUtil;
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
            RequestContextHolderUtil.initContext();
            boolean isRun = lockRunner.lockCall(RedisKeyUtil.getGlobalJobLockKey(this.getClass().getSimpleName()), () -> {
                if (this.getPeriodSeconds() > 0) {
                    String key = this.getJobKey();
                    RBucket<Long> rBucket = redissonClient.getBucket(key);
                    if (rBucket.isExists()) {
                        return false;
                    }
                    rBucket.set(System.currentTimeMillis(), Math.max(1, this.getPeriodSeconds()), TimeUnit.SECONDS);
                }
                return true;
            });
            if (isRun) {
                this.doWork();
            }else{
                log.debug("任务正在执行中,忽略本次执行:{}", this.getName());
            }
        } catch (Exception err) {
            log.error("周期任务执行失败.", err);
        }finally {
            RequestContextHolderUtil.clearContext();
            NotifyContextHolderUtil.afterCompletion();
        }
    }

    public void doWork() {
        try {
            log.debug("开始执行周期任务: {}，当前任务周期 {}s", this.getName(), this.getPeriodSeconds());
            this.dispatch();
        } catch (Exception err) {
            log.info("执行周期任务失败: {}", this.getName(), err);
        }
    }

    public int getPeriodSeconds() {
        return 10;
    }

    public int getDelaySeconds() {
        return 10;
    }

    protected String getJobKey() {
        return RedisKeyUtil.getGlobalJobKey(this.getClass().getName());
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
