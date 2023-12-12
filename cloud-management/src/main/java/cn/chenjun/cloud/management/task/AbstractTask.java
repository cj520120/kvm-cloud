package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.management.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
public abstract class AbstractTask implements CommandLineRunner {
    @Autowired
    private ScheduledExecutorService bossExecutor;
    private final String clusterId = UUID.randomUUID().toString();
    @Autowired
    private RedissonClient redissonClient;
    private long lastStartTime = System.currentTimeMillis();

    @Override
    public void run(String... args) throws Exception {
        this.lastStartTime = System.currentTimeMillis() + this.getDelaySeconds();
        this.bossExecutor.scheduleAtFixedRate(this::call, 0, 1, TimeUnit.SECONDS);
    }

    private void call() {
        try {
            String key = RedisKeyUtil.TASK_CLUSTER + this.getClass().getName();
            RBucket<String> rBucket = redissonClient.getBucket(key);
            boolean isCurrentServer;
            if (!rBucket.isExists()) {
                isCurrentServer = rBucket.setIfAbsent(clusterId);
            } else {
                isCurrentServer = rBucket.compareAndSet(clusterId, clusterId);
            }
            if (rBucket.remainTimeToLive() == -1L) {
                rBucket.expire(Duration.ofSeconds(10));
            }
            if (isCurrentServer) {
                if (System.currentTimeMillis() > this.lastStartTime + TimeUnit.SECONDS.toMillis(this.getPeriodSeconds())) {
                    this.lastStartTime = System.currentTimeMillis();
                    log.info("开始执行周期任务: {}", this.getClass().getName());
                    this.dispatch();
                }
            } else {
                this.lastStartTime = System.currentTimeMillis();
            }
        } catch (Exception err) {
            log.error("周期任务执行失败.", err);
        }
    }

    protected int getDelaySeconds() {
        return 10;
    }

    protected int getPeriodSeconds() {
        return 10;
    }

    /**
     * 任务分发
     *
     * @throws Exception
     */
    protected abstract void dispatch() throws Exception;
}
