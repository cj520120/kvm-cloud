package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.management.util.RedisKeyUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
public class BaseController {
    @Autowired
    private RedissonClient redissonClient;

    public <T> T lockRun(LockAction<T> runnable) {
        RLock rLock = redissonClient.getLock(RedisKeyUtil.GLOBAL_LOCK_KEY);
        try {
            rLock.lock(1, TimeUnit.MINUTES);
            return runnable.run();
        } finally {
            try {
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            } catch (Exception ignored) {

            }
        }
    }

    @FunctionalInterface
    public interface LockAction<T> {

        /**
         * 执行
         *
         * @return
         */
        T run();
    }
}
