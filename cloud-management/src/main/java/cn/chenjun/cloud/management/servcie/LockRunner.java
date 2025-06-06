package cn.chenjun.cloud.management.servcie;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class LockRunner {
    @Autowired
    private RedissonClient redissonClient;

    public void lockRun(String key, Runnable runnable) {
        RLock lock = redissonClient.getLock(key);
        try {
            lock.lock(10, TimeUnit.SECONDS);
            runnable.run();
        } catch (Exception err) {
            log.error("执行失败.lock-key:{}", key, err);
        } finally {
            if (lock != null) {
                try {
                    lock.unlock();
                } catch (Exception ignored) {

                }
            }
        }
    }

    public <T> T lockCall(String key, LockAction<T> runnable) {
        RLock lock = null;
        try {
            lock = redissonClient.getLock(key);
            lock.lock(10, TimeUnit.SECONDS);
            return runnable.run();
        } catch (Exception err) {
            log.error("执行失败.lock-key:{}", key, err);
            throw err;
        } finally {
            if (lock != null) {
                try {
                    lock.unlock();
                } catch (Exception ignored) {

                }
            }
        }
    }

    @FunctionalInterface
    public interface LockAction<T> {

        /**
         * 执行
         *
         * @return 结果
         */
        T run();
    }
}
